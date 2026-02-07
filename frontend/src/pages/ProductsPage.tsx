import {FC, useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {useLocation, useSearchParams} from 'react-router-dom';
import {useProducts} from '../hooks/useProducts';
import ProductGrid from '../components/ProductGrid';
import {Product, ProductVariant} from '../types';
import './ProductsPage.css';

interface ProductsPageProps {
  category?: string;
}

const PRIMARY_TABS = [
  { id: 'women', name: 'Women' },
  { id: 'men', name: 'Men' },
  { id: 'kids', name: 'Kids' }
];

const FILTER_GROUPS = [
  {
    id: 'brand',
    label: 'Brand',
    options: [
      { value: '', label: 'All Brands' },
      { value: 'nike', label: 'Nike' },
      { value: 'adidas', label: 'Adidas' },
      { value: 'puma', label: 'Puma' }
    ]
  },
  {
    id: 'size',
    label: 'Size',
    options: [
      { value: '', label: 'All Sizes' },
      { value: '36', label: '36' },
      { value: '37', label: '37' },
      { value: '38', label: '38' },
      { value: '39', label: '39' }
    ]
  },
  {
    id: 'color',
    label: 'Colour',
    options: [
      { value: '', label: 'All Colours' },
      { value: 'black', label: 'Black' },
      { value: 'white', label: 'White' },
      { value: 'blue', label: 'Blue' },
      { value: 'red', label: 'Red' }
    ]
  },
  {
    id: 'qualities',
    label: 'Qualities',
    options: [
      { value: '', label: 'All Qualities' },
      { value: 'premium', label: 'Premium' },
      { value: 'sustainable', label: 'Sustainable' }
    ]
  },
  {
    id: 'price',
    label: 'Price',
    options: [
      { value: '', label: 'All Prices' },
      { value: '0-50', label: '€0 - €50' },
      { value: '50-100', label: '€50 - €100' },
      { value: '100+', label: '€100+' }
    ]
  },
  {
    id: 'collection',
    label: 'Collection',
    options: [
      { value: '', label: 'All Collections' },
      { value: 'new', label: 'New in' },
      { value: 'sale', label: 'Sale' }
    ]
  },
  {
    id: 'material',
    label: 'Material',
    options: [
      { value: '', label: 'All Materials' },
      { value: 'leather', label: 'Leather' },
      { value: 'canvas', label: 'Canvas' },
      { value: 'synthetic', label: 'Synthetic' }
    ]
  },
  {
    id: 'heel',
    label: 'Type of heel',
    options: [
      { value: '', label: 'All Heel Types' },
      { value: 'flat', label: 'Flat' },
      { value: 'block', label: 'Block' }
    ]
  },
  {
    id: 'shoeWidth',
    label: 'Shoe width',
    options: [
      { value: '', label: 'All Widths' },
      { value: 'narrow', label: 'Narrow' },
      { value: 'regular', label: 'Regular' },
      { value: 'wide', label: 'Wide' }
    ]
  },
  {
    id: 'toe',
    label: 'Toe',
    options: [
      { value: '', label: 'All Toes' },
      { value: 'round', label: 'Round' },
      { value: 'pointed', label: 'Pointed' }
    ]
  }
];

const ProductsPage: FC<ProductsPageProps> = ({ category: propCategory }) => {
  const { products, loading, error, fetchProducts, searchProducts } = useProducts();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const [activeCategory, setActiveCategory] = useState<string>('books');
  const [activeSegment, setActiveSegment] = useState<string>('women');
  const [currentPage, setCurrentPage] = useState(1);
  const [sortBy, setSortBy] = useState<'name' | 'price' | 'rating'>('name');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [activeFilter, setActiveFilter] = useState<string | null>(null);
  const [selectedFilters, setSelectedFilters] = useState({
    size: '',
    brand: '',
    price: '',
    color: '',
    qualities: '',
    collection: '',
    material: '',
    heel: '',
    shoeWidth: '',
    toe: ''
  });
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [allProducts, setAllProducts] = useState<Product[]>([]);
  const seenSkusRef = useRef<Set<string>>(new Set());
  const requestKeyRef = useRef<string>('');
  const loadingMoreStartedAtRef = useRef<number>(0);
  const loadingMoreTimerRef = useRef<number | null>(null);
  const limit = 16;
  const filtersKey = useMemo(() => `products-filters:${location.pathname}`, [location.pathname]);
  const hasHydratedFilters = useRef(false);

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
      if (PRIMARY_TABS.some(tab => tab.id === urlCategory)) {
        setActiveSegment(urlCategory);
      }
    }
  }, [searchParams]);

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

  const handleToggleWishlist = useCallback((productId: string) => {
    console.log('Toggling wishlist for product:', productId);
  }, []);

  const handleQuickView = useCallback((product: Product) => {
    console.log('Quick view for product:', product.title);
  }, []);

  const handleSortChange = (value: string) => {
    const [newSortBy, newSortOrder] = value.split('-') as ['name' | 'price' | 'rating', 'asc' | 'desc'];
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

  const sortedProducts = useMemo(() => {
    return [...allProducts].sort((a, b) => {
      let aValue: string | number = '';
      let bValue: string | number = '';

      switch (sortBy) {
        case 'name':
          aValue = a.title.toLowerCase();
          bValue = b.title.toLowerCase();
          break;
        case 'price':
          aValue = a.price;
          bValue = b.price;
          break;
        case 'rating':
          aValue = a.rating || 0;
          bValue = b.rating || 0;
          break;
      }

      if (sortOrder === 'asc') {
        return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
      }
      return aValue > bValue ? -1 : aValue < bValue ? 1 : 0;
    });
  }, [allProducts, sortBy, sortOrder]);

  const filteredProducts = useMemo(() => {
    const hashSku = (sku: string) => {
      let h = 0;
      for (let i = 0; i < sku.length; i += 1) {
        h = (h * 31 + sku.charCodeAt(i)) >>> 0;
      }
      return h;
    };

    const getFacet = (p: Product) => {
      const seed = hashSku(p.sku);
      const material = ['leather', 'canvas', 'synthetic'][seed % 3];
      const heel = ['flat', 'block'][seed % 2];
      const shoeWidth = ['narrow', 'regular', 'wide'][seed % 3];
      const toe = ['round', 'pointed'][seed % 2];
      const sustainable = seed % 7 === 0;
      const premium = (p.isFeatured ?? false) || p.price >= 100 || seed % 5 === 0;

      return { material, heel, shoeWidth, toe, sustainable, premium };
    };

    const parsePriceRange = (value: string) => {
      if (!value) return null;
      if (value.endsWith('+')) {
        const min = Number(value.slice(0, -1));
        return { min, max: null as number | null };
      }
      const [minStr, maxStr] = value.split('-');
      const min = Number(minStr);
      const max = Number(maxStr);
      if (Number.isFinite(min) && Number.isFinite(max)) return { min, max };
      return null;
    };

    const matches = (p: Product) => {
      const f = selectedFilters;
      const facet = getFacet(p);

      if (f.brand) {
        const brand = (p.brand || '').toLowerCase();
        if (brand !== f.brand.toLowerCase()) return false;
      }

      if (f.size) {
        const hasSize = (p.variants || []).some(v => v.type === 'size' && v.value === f.size);
        if (!hasSize) return false;
      }

      if (f.color) {
        const wanted = f.color.toLowerCase();
        const aliases = wanted === 'blue' ? new Set(['blue', 'navy']) : new Set([wanted]);
        const hasColor = (p.variants || []).some(v => v.type === 'color' && aliases.has(v.name.toLowerCase()));
        if (!hasColor) return false;
      }

      if (f.price) {
        const range = parsePriceRange(f.price);
        if (range) {
          if (p.price < range.min) return false;
          if (range.max != null && p.price > range.max) return false;
        }
      }

      if (f.collection) {
        if (f.collection === 'new' && !p.isNew) return false;
        if (f.collection === 'sale' && !p.isSale) return false;
      }

      if (f.material && facet.material !== f.material) return false;
      if (f.heel && facet.heel !== f.heel) return false;
      if (f.shoeWidth && facet.shoeWidth !== f.shoeWidth) return false;
      if (f.toe && facet.toe !== f.toe) return false;

      if (f.qualities) {
        if (f.qualities === 'premium' && !facet.premium) return false;
        if (f.qualities === 'sustainable' && !facet.sustainable) return false;
      }

      return true;
    };

    return sortedProducts.filter(matches);
  }, [sortedProducts, selectedFilters]);

  const activeCategoryName = useMemo(() => {
    const match = PRIMARY_TABS.find(tab => tab.id === activeCategory);
    return match?.name || activeCategory.charAt(0).toUpperCase() + activeCategory.slice(1);
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

        <div className="segment-tabs">
          {PRIMARY_TABS.map(tab => (
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
              <div className="filter-buttons" aria-label="Filters">
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

              <div className="filter-actions">
                <button className="filter-show-all" type="button">
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

          <div className="product-count">
            <span>{filteredProducts.length.toLocaleString()} items</span>
            <span className="info-icon">i</span>
          </div>

          <div className="products-content">
            {error ? (
              <div className="error-container">
                <p>{error}</p>
              </div>
            ) : (
              <ProductGrid
                products={filteredProducts}
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
