import { FC, useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useProducts } from '../hooks/useProducts';
import ProductCard from '../components/ProductCard';
import { apiClient } from '../services/api';
import { Category } from '../types';
import './ProductsPage.css';

interface ProductsPageProps {
  category?: string;
}

const ProductsPage: FC<ProductsPageProps> = ({ category: propCategory }) => {
  const { products, loading, error, fetchProducts, searchProducts } = useProducts();
  const [searchParams, setSearchParams] = useSearchParams();
  const [activeCategory, setActiveCategory] = useState<string>('clothing');
  const [currentPage, setCurrentPage] = useState(1);
  const [sortBy, setSortBy] = useState<'name' | 'price' | 'rating'>('name');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [showAllFilters, setShowAllFilters] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [activeFilter, setActiveFilter] = useState<string | null>(null);
  const [selectedFilters, setSelectedFilters] = useState({
    size: '',
    brand: '',
    price: '',
    color: '',
    material: '',
    pattern: '',
    length: ''
  });
  const limit = 16;

  // Sidebar categories
  const sidebarCategories = [
    { id: 'clothing', name: 'Clothing', active: true },
    { id: 'dresses', name: 'Dresses' },
    { id: 'knitwear', name: 'Knitwear & Cardigans' },
    { id: 'sweatshirts', name: 'Sweatshirts & Hoodies' },
    { id: 'trousers', name: 'Trousers' },
    { id: 'jeans', name: 'Jeans' },
    { id: 'jackets', name: 'Jackets & Blazers' },
    { id: 'coats', name: 'Coats' },
    { id: 'tops', name: 'T-shirts & tops' },
    { id: 'shirts', name: 'Shirts & Blouses' },
    { id: 'skirts', name: 'Skirts' },
    { id: 'sportswear', name: 'Sportswear' },
    { id: 'jumpsuits', name: 'Jumpsuits' },
    { id: 'swimwear', name: 'Swimwear' },
    { id: 'shorts', name: 'Shorts' },
    { id: 'underwear', name: 'Underwear' },
    { id: 'nightwear', name: 'Nightwear & Loungewear' },
    { id: 'socks', name: 'Socks & Tights' },
    { id: 'sale', name: 'Sale' }
  ];

  // Initialize search query and category from URL parameters
  useEffect(() => {
    const urlCategory = searchParams.get('category');
    
    if (urlCategory) {
      setActiveCategory(urlCategory);
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

  // Load products when category, page, or search query changes
  useEffect(() => {
    const urlSearchQuery = searchParams.get('search');
    const urlCategory = searchParams.get('category');
    
    if (urlSearchQuery) {
      const offset = (currentPage - 1) * limit;
      searchProducts(urlSearchQuery, limit, offset);
    } else {
      const categoryToUse = urlCategory || propCategory || activeCategory;
      const offset = (currentPage - 1) * limit;
      fetchProducts(categoryToUse, limit, offset);
    }
  }, [searchParams, propCategory, activeCategory, currentPage, fetchProducts, searchProducts]);

  const handleCategoryClick = (categoryId: string) => {
    setActiveCategory(categoryId);
    setSearchParams({ category: categoryId });
    setCurrentPage(1);
  };

  const handleSortChange = (value: string) => {
    const [newSortBy, newSortOrder] = value.split('-') as ['name' | 'price' | 'rating', 'asc' | 'desc'];
    setSortBy(newSortBy);
    setSortOrder(newSortOrder);
    setActiveFilter(null); // Close dropdown after selection
  };

  const handleFilterChange = (filterType: string, value: string) => {
    setSelectedFilters(prev => ({
      ...prev,
      [filterType]: value
    }));
    setActiveFilter(null); // Close dropdown after selection
  };

  const toggleFilterDropdown = (filterType: string) => {
    setActiveFilter(activeFilter === filterType ? null : filterType);
  };

  // Sort products locally
  const sortedProducts = [...products].sort((a, b) => {
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
    } else {
      return aValue > bValue ? -1 : aValue < bValue ? 1 : 0;
    }
  });

  return (
    <div className="products-page">
      <div className="products-container">
        {/* Mobile Menu Button */}
        <button 
          className="mobile-menu-btn"
          onClick={() => setSidebarOpen(!sidebarOpen)}
          aria-label="Toggle menu"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <line x1="3" y1="12" x2="21" y2="12"></line>
            <line x1="3" y1="18" x2="21" y2="18"></line>
          </svg>
        </button>

        {/* Sidebar Navigation */}
        <aside className={`products-sidebar ${sidebarOpen ? 'open' : ''}`}>
          <nav className="sidebar-nav">
            {sidebarCategories.map((category) => (
              <button
                key={category.id}
                onClick={() => {
                  handleCategoryClick(category.id);
                  setSidebarOpen(false); // Close sidebar on mobile after selection
                }}
                className={`sidebar-category ${activeCategory === category.id ? 'active' : ''}`}
              >
                {category.name}
              </button>
            ))}
          </nav>
        </aside>

        {/* Sidebar Overlay for Mobile */}
        {sidebarOpen && <div className="sidebar-overlay" onClick={() => setSidebarOpen(false)} />}

        {/* Main Content */}
        <main className="products-main">
          {/* Breadcrumb */}
          <div className="breadcrumb">
            <span>Women</span>
            <span className="breadcrumb-separator">›</span>
            <span>Clothing</span>
          </div>

          {/* Page Title */}
          <h1 className="page-title">
            {searchParams.get('search') 
              ? `Search Results for "${searchParams.get('search')}"` 
              : 'Clothing for Women'
            }
          </h1>

          {/* Filter Bar */}
          <div className="filter-bar">
            <div className="filter-buttons">
              {/* Sort By Filter */}
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
                    <button onClick={() => handleSortChange('name-asc')} className={sortBy === 'name' && sortOrder === 'asc' ? 'selected' : ''}>
                      Most Popular
                      {sortBy === 'name' && sortOrder === 'asc' && <span className="checkmark">✓</span>}
                    </button>
                    <button onClick={() => handleSortChange('name-desc')} className={sortBy === 'name' && sortOrder === 'desc' ? 'selected' : ''}>
                      Newest
                    </button>
                    <button onClick={() => handleSortChange('price-asc')} className={sortBy === 'price' && sortOrder === 'asc' ? 'selected' : ''}>
                      Lowest Price
                    </button>
                    <button onClick={() => handleSortChange('price-desc')} className={sortBy === 'price' && sortOrder === 'desc' ? 'selected' : ''}>
                      Highest Price
                    </button>
                    <button onClick={() => handleSortChange('rating-desc')} className={sortBy === 'rating' && sortOrder === 'desc' ? 'selected' : ''}>
                      Deals
                    </button>
                  </div>
                )}
              </div>

              {/* Brand Filter */}
              <div className="filter-dropdown">
                <button 
                  className={`filter-btn ${activeFilter === 'brand' ? 'active' : ''}`}
                  onClick={() => toggleFilterDropdown('brand')}
                >
                  Brand
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="6,9 12,15 18,9"></polyline>
                  </svg>
                </button>
                {activeFilter === 'brand' && (
                  <div className="filter-dropdown-menu">
                    <button onClick={() => handleFilterChange('brand', '')} className={selectedFilters.brand === '' ? 'selected' : ''}>
                      All Brands
                    </button>
                    <button onClick={() => handleFilterChange('brand', 'culture')} className={selectedFilters.brand === 'culture' ? 'selected' : ''}>
                      Culture
                    </button>
                    <button onClick={() => handleFilterChange('brand', 'elena-miro')} className={selectedFilters.brand === 'elena-miro' ? 'selected' : ''}>
                      Elena Miró
                    </button>
                    <button onClick={() => handleFilterChange('brand', 'vera-mont')} className={selectedFilters.brand === 'vera-mont' ? 'selected' : ''}>
                      Vera Mont
                    </button>
                  </div>
                )}
              </div>

              {/* Color Filter */}
              <div className="filter-dropdown">
                <button 
                  className={`filter-btn ${activeFilter === 'color' ? 'active' : ''}`}
                  onClick={() => toggleFilterDropdown('color')}
                >
                  Colour
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="6,9 12,15 18,9"></polyline>
                  </svg>
                </button>
                {activeFilter === 'color' && (
                  <div className="filter-dropdown-menu">
                    <button onClick={() => handleFilterChange('color', '')} className={selectedFilters.color === '' ? 'selected' : ''}>
                      All Colors
                    </button>
                    <button onClick={() => handleFilterChange('color', 'black')} className={selectedFilters.color === 'black' ? 'selected' : ''}>
                      Black
                    </button>
                    <button onClick={() => handleFilterChange('color', 'white')} className={selectedFilters.color === 'white' ? 'selected' : ''}>
                      White
                    </button>
                    <button onClick={() => handleFilterChange('color', 'blue')} className={selectedFilters.color === 'blue' ? 'selected' : ''}>
                      Blue
                    </button>
                    <button onClick={() => handleFilterChange('color', 'red')} className={selectedFilters.color === 'red' ? 'selected' : ''}>
                      Red
                    </button>
                  </div>
                )}
              </div>

              {/* Price Filter */}
              <div className="filter-dropdown">
                <button 
                  className={`filter-btn ${activeFilter === 'price' ? 'active' : ''}`}
                  onClick={() => toggleFilterDropdown('price')}
                >
                  Price
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="6,9 12,15 18,9"></polyline>
                  </svg>
                </button>
                {activeFilter === 'price' && (
                  <div className="filter-dropdown-menu">
                    <button onClick={() => handleFilterChange('price', '')} className={selectedFilters.price === '' ? 'selected' : ''}>
                      All Prices
                    </button>
                    <button onClick={() => handleFilterChange('price', '0-25')} className={selectedFilters.price === '0-25' ? 'selected' : ''}>
                      €0 - €25
                    </button>
                    <button onClick={() => handleFilterChange('price', '25-50')} className={selectedFilters.price === '25-50' ? 'selected' : ''}>
                      €25 - €50
                    </button>
                    <button onClick={() => handleFilterChange('price', '50-100')} className={selectedFilters.price === '50-100' ? 'selected' : ''}>
                      €50 - €100
                    </button>
                    <button onClick={() => handleFilterChange('price', '100+')} className={selectedFilters.price === '100+' ? 'selected' : ''}>
                      €100+
                    </button>
                  </div>
                )}
              </div>

              {/* Campaigns Filter */}
              <div className="filter-dropdown">
                <button 
                  className={`filter-btn ${activeFilter === 'campaigns' ? 'active' : ''}`}
                  onClick={() => toggleFilterDropdown('campaigns')}
                >
                  Campaigns
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="6,9 12,15 18,9"></polyline>
                  </svg>
                </button>
                {activeFilter === 'campaigns' && (
                  <div className="filter-dropdown-menu">
                    <button onClick={() => handleFilterChange('campaigns', '')} className={selectedFilters.length === '' ? 'selected' : ''}>
                      All Items
                    </button>
                    <button onClick={() => handleFilterChange('campaigns', 'sale')} className={selectedFilters.length === 'sale' ? 'selected' : ''}>
                      Sale
                    </button>
                    <button onClick={() => handleFilterChange('campaigns', 'new')} className={selectedFilters.length === 'new' ? 'selected' : ''}>
                      New arrivals
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* Product Count */}
            <div className="product-count">
              <span>{products.length} items</span>
            </div>
          </div>

          {/* Products Grid */}
          <div className="products-content">
            {loading ? (
              <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>Loading products...</p>
              </div>
            ) : error ? (
              <div className="error-container">
                <p>{error}</p>
              </div>
            ) : (
              <div className="products-grid">
                {sortedProducts.map((product) => (
                  <div key={product.sku} className="product-grid-item">
                    <ProductCard product={product} />
                  </div>
                ))}
              </div>
            )}
          </div>
        </main>
      </div>
    </div>
  );
};

export default ProductsPage;
