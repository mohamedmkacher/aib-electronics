package com.aib.aib_backend.service.impl;

import com.aib.aib_backend.dto.request.ProductRequest;
import com.aib.aib_backend.dto.request.ProductSearchRequest;
import com.aib.aib_backend.dto.response.ProductListResponse;
import com.aib.aib_backend.dto.response.ProductResponse;
import com.aib.aib_backend.exception.DuplicateResourceException;
import com.aib.aib_backend.exception.ResourceNotFoundException;
import com.aib.aib_backend.model.Cart;
import com.aib.aib_backend.model.Category;
import com.aib.aib_backend.model.Product;
import com.aib.aib_backend.repository.CartRepository;
import com.aib.aib_backend.repository.CategoryRepository;
import com.aib.aib_backend.repository.OrderItemRepository;
import com.aib.aib_backend.repository.ProductRepository;
import com.aib.aib_backend.service.EmailService;
import com.aib.aib_backend.service.ProductService;
import com.aib.aib_backend.util.SlugUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;
    private final EmailService emailService;
    private final OrderItemRepository orderItemRepository;
    private final TransactionTemplate countTransactionTemplate;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, CartRepository cartRepository, EmailServiceImpl emailService, OrderItemRepository orderItemRepository, PlatformTransactionManager transactionManager) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cartRepository = cartRepository;
        this.emailService = emailService;
        this.orderItemRepository = orderItemRepository;
        this.countTransactionTemplate = new TransactionTemplate(transactionManager);
        this.countTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        if (productRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Product with name '" + request.getName() + "' already exists");
        }

        String sku = generateSku(request.getName());
        if (productRepository.existsBySku(sku)) {
            sku = generateSku(request.getName() + " " + UUID.randomUUID().toString().substring(0, 4));
        }

        String slug = SlugUtil.generateSlug(request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .sku(sku)
                .description(request.getDescription())
                .brand(request.getBrand())
                .model(request.getModel())
                .price(request.getPrice())
                .discount(request.getDiscount() != null ? request.getDiscount() : 0)
                .stockQuantity(request.getStockQuantity())
                .category(category)
                .mainImageUrl(request.getMainImageUrl())
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        try {
            Product savedProduct = productRepository.save(product);
            log.info("Product created successfully with ID: {}", savedProduct.getId());
            return mapToResponse(savedProduct);
        } catch (DataIntegrityViolationException e) {
            log.error("Error creating product: {}", e.getMessage());
            throw new DuplicateResourceException("Product with this SKU or slug already exists");
        }
    }

    private String generateSku(String productName) {
        String sanitizedName = productName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return (sanitizedName.length() > 4 ? sanitizedName.substring(0, 4) : sanitizedName) + "-" + timestamp;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        int oldStock = product.getStockQuantity();
        boolean wasActive = product.getActive();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        if (!product.getName().equals(request.getName()) &&
                productRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Product with name '" + request.getName() + "' already exists");
        }

        product.setName(request.getName());
        product.setSlug(SlugUtil.generateSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setPrice(request.getPrice());
        product.setDiscount(request.getDiscount() != null ? request.getDiscount() : 0);
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setMainImageUrl(request.getMainImageUrl());

        if (request.getIsFeatured() != null) {
            product.setIsFeatured(request.getIsFeatured());
        }

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        try {
            Product updatedProduct = productRepository.save(product);
            log.info("Product updated successfully: {}", updatedProduct.getId());

            // Notify if product was deactivated
            if (wasActive && !updatedProduct.getActive()) {
                notifyUsersOfProductUnavailability(updatedProduct, true);
            }
            // Notify if stock decreased
            else if (updatedProduct.getStockQuantity() < oldStock) {
                notifyUsersOfProductUnavailability(updatedProduct, false);
            }

            return mapToResponse(updatedProduct);
        } catch (DataIntegrityViolationException e) {
            log.error("Error updating product: {}", e.getMessage());
            throw new DuplicateResourceException("Product with this SKU or slug already exists");
        }
    }

    private void notifyUsersOfProductUnavailability(Product product, boolean isDeactivation) {
        List<Cart> carts = cartRepository.findCartsByProductId(product.getId());
        for (Cart cart : carts) {
            if (cart.getUser() != null) {
                cart.getItems().stream()
                        .filter(item -> item.getProduct().getId().equals(product.getId()))
                        .findFirst()
                        .ifPresent(item -> {
                            if (isDeactivation) {
                                emailService.sendProductDeactivatedEmail(
                                        cart.getUser().getEmail(),
                                        cart.getUser().getFirstName(),
                                        product,
                                        item.getQuantity()
                                );
                            } else if (item.getQuantity() > product.getStockQuantity()) {
                                emailService.sendStockAlertEmail(
                                        cart.getUser().getEmail(),
                                        cart.getUser().getFirstName(),
                                        product,
                                        item.getQuantity()
                                );
                            }
                        });
            }
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        if (orderItemRepository.existsByProductId(id)) {
            throw new IllegalStateException("Cannot delete product because it is part of an existing order.");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        productRepository.delete(product);
        log.info("Product deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public ProductResponse toggleProductStatus(Long id) {
        log.info("Toggling status for product ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        boolean wasActive = product.getActive();

        if (!product.getActive()) {
            if (!product.getCategory().getActive()) {
                throw new IllegalStateException("Cannot activate product because its category '" +
                        product.getCategory().getName() + "' is inactive.");
            }
        }

        product.setActive(!product.getActive());
        Product updatedProduct = productRepository.save(product);
        log.info("Product status toggled to: {}", updatedProduct.getActive());

        // Notify if product was deactivated
        if (wasActive && !updatedProduct.getActive()) {
            notifyUsersOfProductUnavailability(updatedProduct, true);
        }

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long id, Integer quantity) {
        log.info("Updating stock for product ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        int oldStock = product.getStockQuantity();
        product.setStockQuantity(quantity);
        Product updatedProduct = productRepository.save(product);
        log.info("Stock updated to: {} for product ID: {}", quantity, id);

        if (updatedProduct.getStockQuantity() < oldStock) {
            notifyUsersOfProductUnavailability(updatedProduct, false);
        }

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public ProductListResponse searchProducts(ProductSearchRequest searchRequest) {
        log.debug("Searching products with filters: {}", searchRequest);
        int page = Math.max(0, searchRequest.getPage() != null ? searchRequest.getPage() : 0);
        int size = Math.max(1, searchRequest.getSize() != null ? searchRequest.getSize() : 12);
        int offset = page * size;
        String sortBy = normalizeSortBy(searchRequest.getSortBy());
        String sortDirection = normalizeSortDirection(searchRequest.getSortDirection());
        String brandFilter = searchRequest.getBrands() != null && !searchRequest.getBrands().isEmpty()
                ? searchRequest.getBrands().get(0) : searchRequest.getBrand();
        Boolean activeOnly = searchRequest.getActive();

        List<Product> products = productRepository.searchProducts(
                searchRequest.getKeyword(),
                searchRequest.getCategoryId(),
                brandFilter,
                searchRequest.getMinPrice(),
                searchRequest.getMaxPrice(),
                searchRequest.getInStock(),
                searchRequest.getHasDiscount(),
                searchRequest.getIsFeatured(),
                activeOnly,
                searchRequest.getStockStatus(),
                sortBy,
                sortDirection,
                size,
                offset
        );

        Long totalCountResult = countTransactionTemplate.execute(status -> productRepository.countSearchProducts(
                searchRequest.getKeyword(),
                searchRequest.getCategoryId(),
                brandFilter,
                searchRequest.getMinPrice(),
                searchRequest.getMaxPrice(),
                searchRequest.getInStock(),
                searchRequest.getHasDiscount(),
                searchRequest.getIsFeatured(),
                activeOnly,
                searchRequest.getStockStatus()
        ));
        long totalCount = totalCountResult != null ? totalCountResult : 0L;


        int totalPages = (int) Math.ceil((double) totalCount / size);
        List<ProductResponse> productResponses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        ProductListResponse.FilterSummary summary = buildFilterSummary(totalCount);

        return ProductListResponse.builder()
                .products(productResponses)
                .totalElements(totalCount)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(size)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .filterSummary(summary)
                .build();
    }

    @Override
    @Transactional
    public List<ProductResponse> getFeaturedProducts(int limit) {
        log.debug("Fetching featured products, limit: {}", limit);
        List<Product> products = productRepository.findFeaturedProducts(limit);
        return products.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ProductResponse> getDiscountedProducts(int limit) {
        log.debug("Fetching discounted products, limit: {}", limit);
        List<Product> products = productRepository.findDiscountedProducts(limit);
        return products.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<String> getAllBrands() {
        log.debug("Fetching all brands");
        return productRepository.findAllBrands();
    }

    @Override
    @Transactional
    public PriceRange getPriceRange(ProductSearchRequest searchRequest) {
        log.debug("Fetching price range with filters: {}", searchRequest);
        String brandFilter = searchRequest.getBrands() != null && !searchRequest.getBrands().isEmpty()
                ? searchRequest.getBrands().get(0) : searchRequest.getBrand();

        List<Object[]> priceRangeRaw = productRepository.getPriceRangeRaw(
                searchRequest.getKeyword(),
                searchRequest.getCategoryId(),
                brandFilter,
                searchRequest.getInStock(),
                searchRequest.getHasDiscount()
        );
        BigDecimal minPrice = BigDecimal.ZERO;
        BigDecimal maxPrice = BigDecimal.ZERO;
        if (!priceRangeRaw.isEmpty()) {
            Object[] row = priceRangeRaw.get(0);
            minPrice = (BigDecimal) Objects.requireNonNullElse(row[0], BigDecimal.ZERO);
            maxPrice = (BigDecimal) Objects.requireNonNullElse(row[1], BigDecimal.ZERO);
        }
        return new PriceRange(minPrice, maxPrice);
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) return "created_at";
        return switch (sortBy.toLowerCase()) {
            case "createdat", "created_at" -> "created_at";
            case "updatedat", "updated_at" -> "updated_at";
            case "price" -> "price";
            case "name" -> "name";
            default -> {
                log.warn("Unknown sortBy '{}'; defaulting to created_at", sortBy);
                yield "created_at";
            }
        };
    }

    private String normalizeSortDirection(String direction) {
        return (direction == null || !direction.equalsIgnoreCase("ASC")) ? "DESC" : "ASC";
    }

    private ProductListResponse.FilterSummary buildFilterSummary(long totalCount) {
        List<String> availableBrands = productRepository.findAllBrands();
        PriceRange priceRange = getPriceRange(new ProductSearchRequest());
        return ProductListResponse.FilterSummary.builder()
                .availableBrands(availableBrands)
                .minPrice(priceRange.minPrice)
                .maxPrice(priceRange.maxPrice)
                .totalProducts((int) totalCount)
                .build();
    }

    @Override
    @Transactional
    public ProductResponse getProductBySlug(String slug) {
        log.debug("Getting product by slug: {}", slug);
        Product product = productRepository.findProductBySlug(slug, true)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse getProductById(Long id) {
        log.debug("Getting product by ID: {}", id);
        Product product = productRepository.findProductById(id, true)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToResponse(product);
    }

    private ProductResponse mapToResponse(Product product) {
        Category category = null;
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            category = categoryRepository.findById(product.getCategory().getId()).orElse(null);
        }
        return mapToResponseWithCategory(product, category);
    }

    private ProductResponse mapToResponseWithCategory(Product product, Category category) {
        BigDecimal currentPrice = product.getCurrentPrice();
        boolean hasDiscount = product.getDiscount() != null && product.getDiscount() > 0;
        Boolean categoryActive = category != null ? category.getActive() : null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .brand(product.getBrand())
                .model(product.getModel())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .categorySlug(category != null ? category.getSlug() : null)
                .price(product.getPrice())
                .discountPercentage(product.getDiscount())
                .currentPrice(currentPrice)
                .hasDiscount(hasDiscount)
                .stockQuantity(product.getStockQuantity())
                .inStock(product.isInStock())
                .mainImageUrl(product.getMainImageUrl())
                .isFeatured(product.getIsFeatured())
                .active(product.getActive())
                .categoryActive(categoryActive)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public List<ProductResponse> getProductsByCategory(Long categoryId, int page, int size) {
        log.debug("Getting products by category: {}, page: {}, size: {}", categoryId, page, size);
        int offset = page * size;
        List<Product> products = productRepository.findProductsByCategory(categoryId, true, size, offset);
        return products.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
}
