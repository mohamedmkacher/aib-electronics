import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, ProductListResponse, ProductRequest, ProductSearchRequest, PriceRange } from '@shared/models/product.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {}

  searchProducts(searchRequest: ProductSearchRequest): Observable<ProductListResponse> {
    return this.http.post<ProductListResponse>(`${this.apiUrl}/search`, searchRequest);
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  getProductBySlug(slug: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/slug/${slug}`);
  }

  getProductsByCategory(categoryId: number, page: number, size: number): Observable<Product[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Product[]>(`${this.apiUrl}/category/${categoryId}`, { params });
  }

  getFeaturedProducts(limit: number): Observable<Product[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<Product[]>(`${this.apiUrl}/featured`, { params });
  }

  getDiscountedProducts(limit: number): Observable<Product[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<Product[]>(`${this.apiUrl}/discounted`, { params });
  }

  getAllBrands(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/brands`);
  }

  getPriceRange(searchRequest: ProductSearchRequest): Observable<PriceRange> {
    let params = new HttpParams();
    if (searchRequest.keyword) params = params.set('keyword', searchRequest.keyword);
    if (searchRequest.categoryId) params = params.set('categoryId', searchRequest.categoryId.toString());
    if (searchRequest.brand) params = params.set('brand', searchRequest.brand);
    if (searchRequest.inStock) params = params.set('inStock', searchRequest.inStock.toString());
    if (searchRequest.hasDiscount) params = params.set('hasDiscount', searchRequest.hasDiscount.toString());
    return this.http.get<PriceRange>(`${this.apiUrl}/price-range`, { params });
  }

  // Admin methods
  createProduct(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, request);
  }

  updateProduct(id: number, request: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, request);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  toggleProductStatus(id: number): Observable<Product> {
    return this.http.patch<Product>(`${this.apiUrl}/${id}/toggle-status`, {});
  }

  updateStock(id: number, quantity: number): Observable<Product> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.patch<Product>(`${this.apiUrl}/${id}/stock`, {}, { params });
  }
}
