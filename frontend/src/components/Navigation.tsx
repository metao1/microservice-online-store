/**
 * Navigation Component
 * Redesigned to match layout across desktop and mobile.
 */

import { FC, useState, useRef, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useCartContext } from '../context/CartContext';
import Badge from './ui/Badge/Badge';
import { Category } from '../types';
import { apiClient } from '../services/api';
import './Navigation.css';

// Icon components
const SearchIcon: FC<{ className?: string }> = ({ className }) => (
  <svg
    className={className}
    width="14"
    height="14"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <circle cx="11" cy="11" r="8" />
    <path d="m21 21-4.35-4.35" />
  </svg>
);

const UserIcon: FC<{ className?: string }> = ({ className }) => (
  <svg
    className={className}
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
    <circle cx="12" cy="7" r="4" />
  </svg>
);

const ShoppingBagIcon: FC<{ className?: string }> = ({ className }) => (
  <svg
    className={className}
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" />
    <path d="M3 6h18" />
    <path d="M16 10a4 4 0 0 1-8 0" />
  </svg>
);

const HeartIcon: FC<{ className?: string }> = ({ className }) => (
  <svg
    className={className}
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
  </svg>
);

const GlobeIcon: FC<{ className?: string }> = ({ className }) => (
  <svg
    className={className}
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <circle cx="12" cy="12" r="10" />
    <path d="M2 12h20" />
    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
  </svg>
);

const ChevronDownIcon: FC<{ className?: string }> = ({ className }) => (
  <svg
    className={className}
    width="14"
    height="14"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <polyline points="6 9 12 15 18 9" />
  </svg>
);

const MenuIcon: FC<{ className?: string }> = ({ className }) => (
  <svg
    className={className}
    width="20"
    height="20"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <line x1="4" x2="20" y1="12" y2="12" />
    <line x1="4" x2="20" y1="6" y2="6" />
    <line x1="4" x2="20" y1="18" y2="18" />
  </svg>
);

const BackIcon: FC<{ className?: string }> = ({ className }) => (
  <svg
    className={className}
    width="18"
    height="18"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <line x1="19" y1="12" x2="5" y2="12" />
    <polyline points="12 19 5 12 12 5" />
  </svg>
);

interface NavigationProps {
  categories?: Category[];
  onSearch?: (query: string) => void;
  onCategorySelect?: (categoryId: string) => void;
}

const Navigation: FC<NavigationProps> = ({
  categories = [
    { id: 'women', name: 'Women' },
    { id: 'men', name: 'Men' },
    { id: 'kids', name: 'Kids' },
  ],
  onSearch,
  onCategorySelect,
}) => {
  const { getCartItemCount } = useCartContext();
  const itemCount = getCartItemCount();
  const location = useLocation();
  const navigate = useNavigate();

  const utilityLinks = [
    'Help and contact',
    'Free standard delivery over €29,90 & free returns*',
    '30-day return policy',
    'Gift Cards'
  ];

  const secondaryCategories = [
    { id: 'new-in', name: 'NEW IN' },
    { id: 'clothing', name: 'Clothing' },
    { id: 'shoes', name: 'Shoes' },
    { id: 'sports', name: 'Sports' },
    { id: 'streetwear', name: 'Streetwear' },
    { id: 'accessories', name: 'Accessories' },
    { id: 'beauty', name: 'Beauty' },
    { id: 'designer', name: 'Designer' },
    { id: 'brands', name: 'Brands' },
    { id: 'sale', name: 'Sale %', isSale: true },
    { id: 'pre-owned', name: 'Pre-owned' },
  ];

  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [searchSuggestions, setSearchSuggestions] = useState<string[]>([]);
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isMobileSearchActive, setIsMobileSearchActive] = useState(false);
  const [isMobileView, setIsMobileView] = useState(() => {
    if (typeof window === 'undefined') return false;
    return window.matchMedia('(max-width: 900px)').matches;
  });

  // Refs
  const desktopSearchRef = useRef<HTMLInputElement>(null);
  const mobileSearchRef = useRef<HTMLInputElement>(null);

  const isActive = (path: string) => location.pathname === path;

  // Handle search functionality
  const handleSearchChange = async (value: string) => {
    setSearchQuery(value);

    if (value.length > 2) {
      setIsLoadingSuggestions(true);
      try {
        const suggestions = await apiClient.searchProducts(value, 5, 0);
        const suggestionTitles = suggestions.map(product => product.title).slice(0, 5);
        setSearchSuggestions(suggestionTitles);
      } catch (error) {
        console.error('Failed to fetch search suggestions:', error);
        setSearchSuggestions([]);
      } finally {
        setIsLoadingSuggestions(false);
      }
    } else {
      setSearchSuggestions([]);
      setIsLoadingSuggestions(false);
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      onSearch?.(searchQuery.trim());
      navigate(`/products?search=${encodeURIComponent(searchQuery.trim())}`);
      setIsSearchFocused(false);
      setSearchSuggestions([]);
      setIsMobileSearchActive(false);
    }
  };

  const handleSuggestionClick = (suggestion: string) => {
    setSearchQuery(suggestion);
    onSearch?.(suggestion);
    navigate(`/products?search=${encodeURIComponent(suggestion)}`);
    setIsSearchFocused(false);
    setSearchSuggestions([]);
    setIsMobileSearchActive(false);
  };

  const handleCategoryClick = (category: Category) => {
    onCategorySelect?.(category.id);
    navigate(`/products?category=${category.id}`);
    setIsMobileMenuOpen(false);
  };

  const handleSearchFocus = (isMobile: boolean) => {
    setIsSearchFocused(true);
    if (isMobile) {
      setIsMobileSearchActive(true);
    }
  };

  const handleMobileSearchBack = () => {
    setIsMobileSearchActive(false);
    setIsSearchFocused(false);
    setSearchSuggestions([]);
    if (mobileSearchRef.current) {
      mobileSearchRef.current.blur();
    }
  };

  // Close mobile menu on route change
  useEffect(() => {
    setIsMobileMenuOpen(false);
    setIsMobileSearchActive(false);
    setIsSearchFocused(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!isMobileView) {
      document.body.style.overflow = '';
      return;
    }
    document.body.style.overflow = isMobileSearchActive ? 'hidden' : '';
    return () => {
      document.body.style.overflow = '';
    };
  }, [isMobileSearchActive, isMobileView]);

  // Keep mobile-only UI in sync when resizing across breakpoints
  useEffect(() => {
    if (typeof window === 'undefined') return;
    const mediaQuery = window.matchMedia('(max-width: 900px)');

    const handleChange = (event: MediaQueryListEvent | MediaQueryList) => {
      const matches = 'matches' in event ? event.matches : event.matches;
      setIsMobileView(matches);
      if (!matches) {
        setIsMobileSearchActive(false);
        setIsSearchFocused(false);
        setSearchSuggestions([]);
      }
    };

    handleChange(mediaQuery);
    if (typeof mediaQuery.addEventListener === 'function') {
      mediaQuery.addEventListener('change', handleChange);
      return () => mediaQuery.removeEventListener('change', handleChange);
    }

    mediaQuery.addListener(handleChange);
    return () => mediaQuery.removeListener(handleChange);
  }, []);

  return (
    <header className={`navigation ${isSearchFocused && !isMobileView ? 'search-focused' : ''}`} data-testid="navbar">
      {/* Utility Bar */}
      <div className="utility-bar">
        <div className="utility-bar-container">
          {utilityLinks.map((item) => (
            <span key={item} className="utility-item">{item}</span>
          ))}
        </div>
      </div>

      {/* Main Navigation */}
      <div className="nav-shell">
        <div className="nav-row">
          <div className="nav-brand">
            <Link to="/" className="nav-brand-logo" data-testid="nav-brand-logo">
              <span className="brand-mark" aria-hidden="true"></span>
              <span className="brand-text">ModernStore</span>
            </Link>
          </div>

          <div className="nav-right">
            <div className="nav-icons">
              <button className="lang-toggle" type="button">EN</button>
              <button className="icon-button" type="button" aria-label="Change region">
                <GlobeIcon />
              </button>
              <Link
                to="/account"
                className={`icon-link ${isActive('/account') ? 'active' : ''}`}
                data-testid="account-link"
              >
                <UserIcon />
              </Link>
              <Link
                to="/wishlist"
                className={`icon-link ${isActive('/wishlist') ? 'active' : ''}`}
                data-testid="wishlist-link"
              >
                <HeartIcon />
              </Link>
              <Link
                to="/cart"
                className={`icon-link ${isActive('/cart') ? 'active' : ''}`}
                data-testid="cart-link"
              >
                <ShoppingBagIcon />
                {itemCount > 0 && (
                  <Badge
                    variant="secondary"
                    size="sm"
                    count={itemCount}
                    className="cart-badge"
                    data-testid="cart-badge"
                  />
                )}
              </Link>
            </div>

            {/* Desktop Search */}
            <div className="nav-search nav-search-desktop">
              <form onSubmit={handleSearchSubmit} className="search-form">
                <div className="search-input-wrapper">
                  <SearchIcon className="search-icon" />
                  <input
                    ref={desktopSearchRef}
                    type="text"
                    placeholder="Search"
                    value={searchQuery}
                    onChange={(e) => handleSearchChange(e.target.value)}
                    onFocus={() => handleSearchFocus(false)}
                    onBlur={() => setTimeout(() => setIsSearchFocused(false), 200)}
                    className="search-input"
                    data-testid="search-input"
                    role="combobox"
                    aria-label="Search products"
                    aria-haspopup="listbox"
                    aria-expanded={isSearchFocused && (searchSuggestions.length > 0 || searchQuery.length > 2)}
                    autoComplete="off"
                    spellCheck="false"
                  />
                </div>

                {isSearchFocused && (
                  <div className="search-suggestions" data-testid="search-suggestions">
                    {isLoadingSuggestions ? (
                      <div className="search-suggestion loading-suggestion">
                        <SearchIcon className="suggestion-icon" />
                        Searching...
                      </div>
                    ) : searchSuggestions.length > 0 ? (
                      searchSuggestions.map((suggestion, index) => (
                        <button
                          key={index}
                          type="button"
                          className="search-suggestion"
                          onClick={() => handleSuggestionClick(suggestion)}
                          data-testid={`search-suggestion-${index}`}
                        >
                          <SearchIcon className="suggestion-icon" />
                          {suggestion}
                        </button>
                      ))
                    ) : searchQuery.length > 2 ? (
                      <div className="search-suggestion no-results">
                        <SearchIcon className="suggestion-icon" />
                        No suggestions found
                      </div>
                    ) : null}
                  </div>
                )}
              </form>
            </div>
          </div>
        </div>

        {/* Secondary Categories */}
        <div className="nav-secondary" role="navigation" aria-label="Secondary navigation">
          {secondaryCategories.map((category) => (
            <button
              key={category.id}
              className={`nav-secondary-link ${category.isSale ? 'sale' : ''}`}
              onClick={() => handleCategoryClick({ id: category.id, name: category.name })}
            >
              {category.name}
            </button>
          ))}
        </div>

        {/* Mobile search row */}
        {!isMobileSearchActive && (
          <div className="nav-search-row">
            <button
              className="mobile-menu-toggle"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              aria-expanded={isMobileMenuOpen}
              aria-label="Toggle mobile menu"
              data-testid="mobile-menu-toggle"
            >
              <MenuIcon />
            </button>

            <div className="nav-search nav-search-mobile">
              <form onSubmit={handleSearchSubmit} className="search-form">
                <div className="search-input-wrapper">
                  <SearchIcon className="search-icon" />
                  <input
                    ref={mobileSearchRef}
                    type="text"
                    placeholder="Search"
                    value={searchQuery}
                    onChange={(e) => handleSearchChange(e.target.value)}
                    onFocus={() => handleSearchFocus(true)}
                    className="search-input"
                    data-testid="search-input-mobile"
                    role="combobox"
                    aria-label="Search products"
                    autoComplete="off"
                    spellCheck="false"
                  />
                </div>
              </form>
            </div>
          </div>
        )}
      </div>

      {/* Mobile Search Active Bar */}
      {isMobileSearchActive && isMobileView && (
        <div className="mobile-search-overlay" role="dialog" aria-label="Search">
          <div className="mobile-search-bar">
            <button
              type="button"
              className="mobile-search-back"
              aria-label="Back"
              onClick={handleMobileSearchBack}
            >
              <BackIcon />
            </button>
            <button type="button" className="mobile-search-all">
              <span>All</span>
              <ChevronDownIcon />
            </button>
            <div className="mobile-search-input">
              <SearchIcon className="search-icon" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => handleSearchChange(e.target.value)}
                placeholder="Search"
                autoFocus
              />
            </div>
          </div>
          <div className="mobile-search-body" aria-hidden="true"></div>
        </div>
      )}

      {/* Mobile Menu Overlay */}
      {isMobileMenuOpen && (
        <>
          <div
            className="mobile-menu-backdrop"
            onClick={() => setIsMobileMenuOpen(false)}
            aria-hidden="true"
          />
          <div
            className="mobile-menu"
            data-testid="mobile-menu"
            role="dialog"
            aria-modal="true"
            aria-labelledby="mobile-menu-title"
          >
            <div className="mobile-menu-content">
              <span id="mobile-menu-title" className="sr-only">Menu</span>
              <nav className="mobile-nav-categories" role="navigation" aria-label="Mobile navigation">
                {categories.map((category) => (
                  <button
                    key={category.id}
                    onClick={() => handleCategoryClick(category)}
                    className={`mobile-nav-category ${isActive(`/products?category=${category.id}`) ? 'mobile-nav-category-active' : ''}`}
                    data-testid={`mobile-category-${category.id}`}
                  >
                    {category.name}
                  </button>
                ))}
              </nav>

              <div className="mobile-user-actions">
                <Link
                  to="/account"
                  className={`mobile-user-link ${isActive('/account') ? 'mobile-user-link-active' : ''}`}
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  <UserIcon />
                  Account
                </Link>
                <Link
                  to="/wishlist"
                  className={`mobile-user-link ${isActive('/wishlist') ? 'mobile-user-link-active' : ''}`}
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  <HeartIcon />
                  Wishlist
                </Link>
                <Link
                  to="/cart"
                  className={`mobile-user-link ${isActive('/cart') ? 'mobile-user-link-active' : ''}`}
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  <ShoppingBagIcon />
                  Cart {itemCount > 0 && `(${itemCount})`}
                </Link>
              </div>
            </div>
          </div>
        </>
      )}
    </header>
  );
};

export default Navigation;
