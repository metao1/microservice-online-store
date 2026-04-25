import {FC, useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {useLocation, useSearchParams} from 'react-router-dom';
import {useProducts} from '@hooks/useProducts';
import ProductGrid from '../components/ProductGrid';
import {Product, ProductVariant} from '@types';
import { apiClient } from '../services/api';
import { FILTER_GROUPS, createDefaultSelectedFilters } from './products/products.config';
import { ProductSortBy, ProductSortOrder } from './products/products.types';
import {
  applySegmentFilter,
  buildCategoryTabs,
  buildSegmentTabsForCategory,
  filterProducts,
  formatCategoryLabel,
  getProductSearchText,
  sortProducts,
} from './products/products.utils';
import './ProductsPage.css';

interface ProductsPageProps {
  category?: string;
}

const ProductsPage: FC<ProductsPageProps> = ({ category: propCategory }) => {
  const { products, loading, error, fetchProducts, searchProducts } = useProducts();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const [activeCategory, setActiveCategory] = useState<string>('books');
  const [activeSegment, setActiveSegment] = useState<string>('');
  const [availableCategories, setAvailableCategories] = useState<string[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [sortBy, setSortBy] = useState<ProductSortBy>('name');
  const [sortOrder, setSortOrder] = useState<ProductSortOrder>('asc');
  const [activeFilter, setActiveFilter] = useState<string | null>(null);
  const [isAllFiltersOpen, setIsAllFiltersOpen] = useState(false);
  const [canScrollFiltersLeft, setCanScrollFiltersLeft] = useState(false);
  const [canScrollFiltersRight, setCanScrollFiltersRight] = useState(false);
  const [selectedFilters, setSelectedFilters] = useState(createDefaultSelectedFilters());
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [allProducts, setAllProducts] = useState<Product[]>([]);
  const seenSkusRef = useRef<Set<string>>(new Set());
  const requestKeyRef = useRef<string>('');
  const loadingMoreStartedAtRef = useRef<number>(0);
  const loadingMoreTimerRef = useRef<number | null>(null);
  const filterButtonsRef = useRef<HTMLDivElement>(null);
  const limit = 16;
  const filtersKey = useMemo(() => `products-filters:${location.pathname}`, [location.pathname]);
  const hasHydratedFilters = useRef(false);

  const segmentTabs = useMemo(() => buildSegmentTabsForCategory(activeCategory), [activeCategory]);

  const categoryTabs = useMemo(
    () => buildCategoryTabs(availableCategories, activeCategory),
    [availableCategories, activeCategory]
  );

  useEffect(() => {
    if (hasHydratedFilters.current) return;
    const raw = window.sessionStorage.getItem(filtersKey);
    if (!raw) {
      hasHydratedFilters.current = true;
      return;
    }
    try {
      const data = JSON.parse(raw);
      if (data?.sortBy) setSortBy(data.sortBy);
      if (data?.sortOrder) setSortOrder(data.sortOrder);
      if (data?.selectedFilters) {
        setSelectedFilters((prev) => ({ ...prev, ...data.selectedFilters }));
      }
    } catch {
      // ignore corrupt storage
    } finally {
      hasHydratedFilters.current = true;
    }
  }, [filtersKey]);

  useEffect(() => {
    let cancelled = false;
    const loadCategories = async () => {
      try {
        const categories = await apiClient.getCategories(50, 0);
        if (cancelled) return;
        const normalized = categories
          .map((item) => item.category || item.name || '')
          .map((value) => value.trim().toLowerCase())
          .filter(Boolean);
        if (normalized.length > 0) {
          setAvailableCategories(Array.from(new Set(normalized)));
        }
      } catch {
        // Ignore category lookup errors; the page still has sensible fallbacks.
      }
    };
    loadCategories();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!hasHydratedFilters.current) return;
    const payload = {
      sortBy,
      sortOrder,
      selectedFilters
    };
    window.sessionStorage.setItem(filtersKey, JSON.stringify(payload));
  }, [filtersKey, sortBy, sortOrder, selectedFilters]);


  // Initialize search query and category from URL parameters
  useEffect(() => {
    const urlCategory = searchParams.get('category');

    if (urlCategory) {
      setActiveCategory(urlCategory);
    }
  }, [searchParams]);

  useEffect(() => {
    if (!segmentTabs.length) return;
    if (!segmentTabs.some((tab) => tab.id === activeSegment)) {
      setActiveSegment(segmentTabs[0].id);
    }
  }, [segmentTabs, activeSegment]);

  // Close filter dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Element;
      if (!target.closest('.filter-dropdown')) {
        setActiveFilter(null);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  useEffect(() => {
    return () => {
      if (loadingMoreTimerRef.current) {
        window.clearTimeout(loadingMoreTimerRef.current);
      }
    };
  }, []);

  // Load products when category, page, or search query changes
  useEffect(() => {
    const urlSearchQuery = searchParams.get('search');
    const urlCategory = searchParams.get('category');
    const categoryToUse = urlCategory || propCategory || activeCategory;

    // Reset pagination when the query/category changes; otherwise we'd fetch with the old page offset.
    const nextKey = urlSearchQuery ? `search:${urlSearchQuery}` : `category:${categoryToUse}`;
    if (requestKeyRef.current && requestKeyRef.current !== nextKey && currentPage !== 1) {
      requestKeyRef.current = nextKey;
      setCurrentPage(1);
      return;
    }
    requestKeyRef.current = nextKey;

    if (currentPage === 1) {
      setAllProducts([]);
      setHasMore(true);
      seenSkusRef.current = new Set();
    }

    if (urlSearchQuery) {
      const offset = (currentPage - 1) * limit;
      searchProducts(urlSearchQuery, limit, offset);
    } else {
      const offset = (currentPage - 1) * limit;
      fetchProducts(categoryToUse, limit, offset);
    }
  }, [searchParams, propCategory, activeCategory, currentPage, fetchProducts, searchProducts]);

  const handleSegmentClick = (segmentId: string) => {
    setActiveSegment(segmentId);
  };

  const handleCategoryClick = (categoryId: string) => {
    if (categoryId === activeCategory) return;
    setActiveCategory(categoryId);
    setCurrentPage(1);
  };

  // Update aggregated list after each fetch completes.
  // Important: handle the empty-array case to avoid getting stuck in "loadingMore".
  useEffect(() => {
    if (loading) return;

    if (loadingMore) {
      const minVisibleMs = 350;
      const elapsed = Date.now() - loadingMoreStartedAtRef.current;
      const remaining = Math.max(0, minVisibleMs - elapsed);

      if (loadingMoreTimerRef.current) {
        window.clearTimeout(loadingMoreTimerRef.current);
      }

      loadingMoreTimerRef.current = window.setTimeout(() => {
        setLoadingMore(false);
      }, remaining);
    }

    // If the backend errors, stop infinite scroll for this session.
    if (error) {
      setHasMore(false);
      return;
    }

    if (currentPage === 1) {
      setAllProducts(products);
      seenSkusRef.current = new Set(products.map(p => p.sku));
    } else if (products.length > 0) {
      const seen = seenSkusRef.current;
      const uniqueNext = products.filter(p => !seen.has(p.sku));
      uniqueNext.forEach(p => seen.add(p.sku));

      if (uniqueNext.length === 0) {
        // If the backend ignores offset/limit and keeps returning the same items,
        // stop requesting further pages to avoid an infinite loop.
        setHasMore(false);
        return;
      }

      setAllProducts(prev => [...prev, ...uniqueNext]);
    }

    // If the server returns fewer than `limit`, we assume there are no more pages.
    // Use >= for safety in case the backend returns more than requested.
    setHasMore(products.length >= limit);
  }, [loading, error, products, currentPage, limit]);

  const handleLoadMore = useCallback(() => {
    if (!loadingMore && hasMore) {
      loadingMoreStartedAtRef.current = Date.now();
      setLoadingMore(true);
      setCurrentPage(prev => prev + 1);
    }
  }, [loadingMore, hasMore]);

  const handleAddToCart = useCallback((product: Product, selectedVariants?: ProductVariant[]) => {
    console.log('Adding to cart:', product.title, selectedVariants);
  }, []);

  const handleToggleWishlist = useCallback((sku: string) => {
    console.log('Toggling wishlist for product:', sku);
  }, []);

  const handleQuickView = useCallback((product: Product) => {
    console.log('Quick view for product:', product.title);
  }, []);

  const handleSortChange = (value: string) => {
    const [newSortBy, newSortOrder] = value.split('-') as [ProductSortBy, ProductSortOrder];
    setSortBy(newSortBy);
    setSortOrder(newSortOrder);
    setActiveFilter(null);
  };

  const handleFilterChange = (filterType: keyof typeof selectedFilters, value: string) => {
    setSelectedFilters(prev => ({
      ...prev,
      [filterType]: value
    }));
    setActiveFilter(null);
  };

  const toggleFilterDropdown = (filterType: string) => {
    setActiveFilter(activeFilter === filterType ? null : filterType);
  };

  const openAllFilters = () => {
    setIsAllFiltersOpen(true);
  };

  const closeAllFilters = () => {
    setIsAllFiltersOpen(false);
  };

  const resetAllFilters = () => {
    setSelectedFilters(createDefaultSelectedFilters());
    setSortBy('name');
    setSortOrder('asc');
  };

  const updateFilterScrollState = useCallback(() => {
    const strip = filterButtonsRef.current;
    if (!strip) return;
    const maxScrollLeft = strip.scrollWidth - strip.clientWidth;
    setCanScrollFiltersLeft(strip.scrollLeft > 1);
    setCanScrollFiltersRight(maxScrollLeft - strip.scrollLeft > 1);
  }, []);

  const scrollFilters = (direction: 'left' | 'right') => {
    const strip = filterButtonsRef.current;
    if (!strip) return;
    const delta = Math.max(140, Math.floor(strip.clientWidth * 0.7));
    strip.scrollBy({
      left: direction === 'left' ? -delta : delta,
      behavior: 'smooth'
    });
  };

  useEffect(() => {
    updateFilterScrollState();
    window.addEventListener('resize', updateFilterScrollState);
    return () => {
      window.removeEventListener('resize', updateFilterScrollState);
    };
  }, [updateFilterScrollState, activeFilter, allProducts.length]);

  useEffect(() => {
    if (!isAllFiltersOpen) return;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = previousOverflow;
    };
  }, [isAllFiltersOpen]);

  const sortedProducts = useMemo(
    () => sortProducts(allProducts, sortBy, sortOrder),
    [allProducts, sortBy, sortOrder]
  );

  const filteredProducts = useMemo(
    () => filterProducts(sortedProducts, selectedFilters),
    [sortedProducts, selectedFilters]
  );

  const segmentedProducts = useMemo(
    () => applySegmentFilter(filteredProducts, segmentTabs, activeSegment, getProductSearchText),
    [filteredProducts, segmentTabs, activeSegment]
  );

  const activeCategoryName = useMemo(() => {
    if (!activeCategory) {
      return 'Products';
    }
    return formatCategoryLabel(activeCategory);
  }, [activeCategory]);

  return (
    <div className="products-page">
      <div className="products-header">
        <div className="breadcrumb">
          <span>All</span>
          <span className="breadcrumb-separator">›</span>
          <span>{activeCategoryName}</span>
        </div>

        <h1 className="page-title">
          {searchParams.get('search')
            ? `Search Results for "${searchParams.get('search')}"`
            : activeCategoryName
          }
        </h1>

        <div className="mobile-category-tabs" aria-label="Categories">
          {categoryTabs.map((category) => (
            <button
              type="button"
              key={category.id}
              className={`mobile-category-tab ${activeCategory === category.id ? 'active' : ''}`}
              onClick={() => handleCategoryClick(category.id)}
            >
              {category.label}
            </button>
          ))}
        </div>

        <div className="segment-tabs">
          {segmentTabs.map(tab => (
            <button
              key={tab.id}
              className={`segment-tab ${activeSegment === tab.id ? 'active' : ''}`}
              onClick={() => handleSegmentClick(tab.id)}
            >
              {tab.name}
            </button>
          ))}
        </div>
      </div>

      <div className="products-container">
        <main className="products-main">
          <div className="filter-bar">
            <div className="filter-area">
              <div className="filter-strip">
                <button
                  type="button"
                  className="filter-scroll-btn"
                  aria-label="Scroll filters left"
                  onClick={() => scrollFilters('left')}
                  disabled={!canScrollFiltersLeft}
                >
                  ‹
                </button>
                <div
                  className="filter-buttons"
                  aria-label="Filters"
                  ref={filterButtonsRef}
                  onScroll={updateFilterScrollState}
                >
                  <div className="filter-dropdown">
                  <button
                      className={`filter-btn ${activeFilter === 'sort' ? 'active' : ''}`}
                      onClick={() => toggleFilterDropdown('sort')}
                  >
                    Sort by
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polyline points="6,9 12,15 18,9"></polyline>
                    </svg>
                  </button>
                  {activeFilter === 'sort' && (
                      <div className="filter-dropdown-menu">
                        <button onClick={() => handleSortChange('name-asc')}
                                className={sortBy === 'name' && sortOrder === 'asc' ? 'selected' : ''}>
                          Most Popular
                          {sortBy === 'name' && sortOrder === 'asc' && <span className="checkmark">✓</span>}
                        </button>
                        <button onClick={() => handleSortChange('name-desc')}
                                className={sortBy === 'name' && sortOrder === 'desc' ? 'selected' : ''}>
                          Newest
                        </button>
                        <button onClick={() => handleSortChange('price-asc')}
                                className={sortBy === 'price' && sortOrder === 'asc' ? 'selected' : ''}>
                          Lowest Price
                        </button>
                        <button onClick={() => handleSortChange('price-desc')}
                                className={sortBy === 'price' && sortOrder === 'desc' ? 'selected' : ''}>
                          Highest Price
                        </button>
                        <button onClick={() => handleSortChange('rating-desc')}
                                className={sortBy === 'rating' && sortOrder === 'desc' ? 'selected' : ''}>
                          Deals
                        </button>
                      </div>
                  )}
                </div>

                {FILTER_GROUPS.map((filter) => (
                    <div className="filter-dropdown" key={filter.id}>
                      <button
                          className={`filter-btn ${activeFilter === filter.id ? 'active' : ''}`}
                          onClick={() => toggleFilterDropdown(filter.id)}
                      >
                        {filter.label}
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             strokeWidth="2">
                          <polyline points="6,9 12,15 18,9"></polyline>
                        </svg>
                      </button>
                      {activeFilter === filter.id && (
                          <div className="filter-dropdown-menu">
                            {filter.options.map((option) => (
                                <button
                                    key={option.value}
                                    onClick={() => handleFilterChange(filter.id as keyof typeof selectedFilters, option.value)}
                                    className={selectedFilters[filter.id as keyof typeof selectedFilters] === option.value ? 'selected' : ''}
                                >
                                  {option.label}
                                </button>
                            ))}
                          </div>
                      )}
                    </div>
                ))}
                </div>
                <button
                  type="button"
                  className="filter-scroll-btn"
                  aria-label="Scroll filters right"
                  onClick={() => scrollFilters('right')}
                  disabled={!canScrollFiltersRight}
                >
                  ›
                </button>
              </div>

              <div className="filter-actions">
                <button className="filter-show-all" type="button" onClick={openAllFilters}>
                  <span className="filter-icon" aria-hidden="true">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="4" y1="21" x2="4" y2="14"></line>
                      <line x1="4" y1="10" x2="4" y2="3"></line>
                      <line x1="12" y1="21" x2="12" y2="12"></line>
                      <line x1="12" y1="8" x2="12" y2="3"></line>
                      <line x1="20" y1="21" x2="20" y2="16"></line>
                      <line x1="20" y1="12" x2="20" y2="3"></line>
                      <line x1="1" y1="14" x2="7" y2="14"></line>
                      <line x1="9" y1="8" x2="15" y2="8"></line>
                      <line x1="17" y1="16" x2="23" y2="16"></line>
                    </svg>
                  </span>
                  Show all filters
                </button>
              </div>
            </div>
          </div>

          {isAllFiltersOpen && (
            <>
              <button
                type="button"
                className="all-filters-backdrop"
                aria-label="Close all filters"
                onClick={closeAllFilters}
              />
              <section className="all-filters-drawer" role="dialog" aria-modal="true" aria-label="All filters">
                <header className="all-filters-header">
                  <h2>All filters</h2>
                  <button type="button" onClick={closeAllFilters} className="all-filters-close">Close</button>
                </header>
                <div className="all-filters-groups">
                  {FILTER_GROUPS.map((filter) => (
                    <div className="all-filters-group" key={`drawer-${filter.id}`}>
                      <h3>{filter.label}</h3>
                      <div className="all-filters-options">
                        {filter.options.map((option) => (
                          <button
                            type="button"
                            key={`drawer-${filter.id}-${option.value || 'all'}`}
                            className={selectedFilters[filter.id as keyof typeof selectedFilters] === option.value ? 'selected' : ''}
                            onClick={() => handleFilterChange(filter.id as keyof typeof selectedFilters, option.value)}
                          >
                            {option.label}
                          </button>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
                <footer className="all-filters-footer">
                  <button type="button" className="all-filters-reset" onClick={resetAllFilters}>
                    Reset
                  </button>
                  <button type="button" className="all-filters-apply" onClick={closeAllFilters}>
                    Apply
                  </button>
                </footer>
              </section>
            </>
          )}

          <div className="product-count">
            <span>{segmentedProducts.length.toLocaleString()} items</span>
            <span className="info-icon">i</span>
          </div>

          <div className="products-content">
            {error ? (
              <div className="error-container">
                <p>{error}</p>
              </div>
            ) : (
              <ProductGrid
                products={segmentedProducts}
                loading={loading && currentPage === 1}
                loadingMore={loadingMore}
                hasMore={hasMore}
                onLoadMore={handleLoadMore}
                onAddToCart={handleAddToCart}
                onToggleWishlist={handleToggleWishlist}
                onQuickView={handleQuickView}
                infiniteScroll={true}
                emptyMessage={searchParams.get('search')
                  ? `No products found for "${searchParams.get('search')}"`
                  : "No products found in this category"
                }
                className="products-page-grid"
              />
            )}
          </div>
        </main>
      </div>
    </div>
  );
};

export default ProductsPage;
