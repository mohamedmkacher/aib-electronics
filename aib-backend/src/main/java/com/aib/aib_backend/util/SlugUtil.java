package com.aib.aib_backend.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-friendly slugs
 */
public class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGES_DASHES = Pattern.compile("(^-|-$)");

    /**
     * Generate a URL-friendly slug from a string
     *
     * Examples:
     * "Dell XPS 15" -> "dell-xps-15"
     * "HP LaserJet Pro M404n" -> "hp-laserjet-pro-m404n"
     * "Laptops & Computers" -> "laptops-computers"
     *
     * @param input The string to convert to a slug
     * @return URL-friendly slug
     */
    public static String generateSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Convert to lowercase
        String slug = input.toLowerCase(Locale.ENGLISH);

        // Normalize unicode characters (remove accents, etc.)
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);

        // Replace whitespace with dashes
        slug = WHITESPACE.matcher(slug).replaceAll("-");

        // Remove all non-word characters except dashes
        slug = NON_LATIN.matcher(slug).replaceAll("");

        // Remove dashes from edges
        slug = EDGES_DASHES.matcher(slug).replaceAll("");

        // Replace multiple consecutive dashes with single dash
        slug = slug.replaceAll("-+", "-");

        return slug;
    }

    /**
     * Generate a unique slug by appending a number if slug already exists
     *
     * @param baseSlug The base slug
     * @param counter The counter to append
     * @return Unique slug with counter
     */
    public static String generateUniqueSlug(String baseSlug, int counter) {
        return baseSlug + "-" + counter;
    }
}
