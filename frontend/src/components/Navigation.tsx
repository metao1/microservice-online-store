/**
 * Modern Navigation Component
 * Professional navigation system with clean header layout, search integration, and cart functionality
 * Based on requirements 1.1, 1.3, 1.5
 */

import { FC, useState, useRef, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useCartContext } from '../context/CartContext';
import Badge from './ui/Badge/Badge';
import { Category } from '../types';
import { apiClient } from '../services/api';
import './Navigation.css';

// Search icon component
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

// User icon component
const UserIcon: FC<{ className?: string }> = ({ className }) => (
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
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
    <circle cx="12" cy="7" r="4" />
  </svg>
);

// Shopping bag icon component
const ShoppingBagIcon: FC<{ className?: string }> = ({ className }) => (
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
    <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" />
    <path d="M3 6h18" />
    <path d="M16 10a4 4 0 0 1-8 0" />
  </svg>
);

// Heart icon component
const HeartIcon: FC<{ className?: string }> = ({ className }) => (
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
    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
  </svg>
);

// Menu icon component
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
    { id: 'shoes', name: 'Shoes' },
    { id: 'accessories', name: 'Accessories' },
    { id: 'beauty', name: 'Beauty' },
    { id: 'sports', name: 'Sports' },
    { id: 'sale', name: 'Sale' },
  ],
  onSearch,
  onCategorySelect,
}) => {
  const { getCartItemCount } = useCartContext();
  const itemCount = getCartItemCount();
  const location = useLocation();
  const navigate = useNavigate();

  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [searchSuggestions, setSearchSuggestions] = useState<string[]>([]);
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isAccountDropdownOpen, setIsAccountDropdownOpen] = useState(false);

  // Refs
  const searchRef = useRef<HTMLInputElement>(null);
  const accountDropdownRef = useRef<HTMLDivElement>(null);
  const dropdownTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const isActive = (path: string) => location.pathname === path;

  // Handle search functionality
  const handleSearchChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const query = e.target.value;
    setSearchQuery(query);

    // Get real search suggestions from API
    if (query.length > 2) {
      setIsLoadingSuggestions(true);
      try {
        const suggestions = await apiClient.searchProducts(query, 5, 0);
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
    }
  };

  const handleSuggestionClick = (suggestion: string) => {
    setSearchQuery(suggestion);
    onSearch?.(suggestion);
    navigate(`/products?search=${encodeURIComponent(suggestion)}`);
    setIsSearchFocused(false);
    setSearchSuggestions([]);
  };

  const handleCategoryClick = (category: Category) => {
    onCategorySelect?.(category.id);
    navigate(`/products?category=${category.id}`);
    setIsMobileMenuOpen(false);
  };

  // Close mobile menu on route change
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location.pathname]);

  // Handle click outside account dropdown
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (accountDropdownRef.current && !accountDropdownRef.current.contains(event.target as Node)) {
        setIsAccountDropdownOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (dropdownTimeoutRef.current) {
        clearTimeout(dropdownTimeoutRef.current);
      }
    };
  }, []);

  // Handle dropdown hover with delay
  const handleDropdownMouseEnter = () => {
    if (dropdownTimeoutRef.current) {
      clearTimeout(dropdownTimeoutRef.current);
    }
    setIsAccountDropdownOpen(true);
  };

  const handleDropdownMouseLeave = () => {
    dropdownTimeoutRef.current = setTimeout(() => {
      setIsAccountDropdownOpen(false);
    }, 300); // Increased to 300ms delay before closing
  };

  return (
    <header className="navigation" data-testid="navbar">
      {/* Top Bar - User Actions (Desktop Only) */}
      <div className="top-bar">
        <div className="top-bar-container">
          <div className="top-bar-spacer"></div>
          
          {/* User Actions */}
          <div className="top-bar-actions">
            {/* Account Link with Dropdown */}
            <div 
              className="account-dropdown-container"
              ref={accountDropdownRef}
              onMouseEnter={handleDropdownMouseEnter}
              onMouseLeave={handleDropdownMouseLeave}
            >
              <Link
                to="/account"
                className={`top-action-link ${isActive('/account') ? 'top-action-link-active' : ''}`}
                data-testid="account-link"
              >
                <UserIcon />
              </Link>
              
              {/* Account Dropdown */}
              {isAccountDropdownOpen && (
                <div 
                  className="account-dropdown"
                  onMouseEnter={handleDropdownMouseEnter}
                  onMouseLeave={handleDropdownMouseLeave}
                >
                  <div className="account-dropdown-content">
                    <Link 
                      to="/account" 
                      className="account-dropdown-item account-main-item"
                      onClick={() => {
                        if (dropdownTimeoutRef.current) {
                          clearTimeout(dropdownTimeoutRef.current);
                        }
                        setIsAccountDropdownOpen(false);
                      }}
                    >
                      Your account
                    </Link>
                    
                    <Link 
                      to="/orders" 
                      className="account-dropdown-item"
                      onClick={() => {
                        if (dropdownTimeoutRef.current) {
                          clearTimeout(dropdownTimeoutRef.current);
                        }
                        setIsAccountDropdownOpen(false);
                      }}
                    >
                      Orders
                    </Link>
                    
                    <Link 
                      to="/returns" 
                      className="account-dropdown-item"
                      onClick={() => {
                        if (dropdownTimeoutRef.current) {
                          clearTimeout(dropdownTimeoutRef.current);
                        }
                        setIsAccountDropdownOpen(false);
                      }}
                    >
                      Return an item
                    </Link>
                    
                    <Link 
                      to="/sizes" 
                      className="account-dropdown-item"
                      onClick={() => {
                        if (dropdownTimeoutRef.current) {
                          clearTimeout(dropdownTimeoutRef.current);
                        }
                        setIsAccountDropdownOpen(false);
                      }}
                    >
                      Your sizes
                    </Link>                                     
                    <Link 
                      to="/appearance" 
                      className="account-dropdown-item"
                      onClick={() => {
                        if (dropdownTimeoutRef.current) {
                          clearTimeout(dropdownTimeoutRef.current);
                        }
                        setIsAccountDropdownOpen(false);
                      }}
                    >
                      Change appearance
                    </Link>
                    
                    <Link 
                      to="/help" 
                      className="account-dropdown-item"
                      onClick={() => {
                        if (dropdownTimeoutRef.current) {
                          clearTimeout(dropdownTimeoutRef.current);
                        }
                        setIsAccountDropdownOpen(false);
                      }}
                    >
                      Help & FAQ
                    </Link>
                    
                    <div className="account-dropdown-divider"></div>
                    
                    <div className="account-dropdown-user">
                      <span className="user-email">Not yaboci8065@aixind.com?</span>
                      <button 
                        className="sign-out-link"
                        onClick={() => {
                          if (dropdownTimeoutRef.current) {
                            clearTimeout(dropdownTimeoutRef.current);
                          }
                          setIsAccountDropdownOpen(false);
                          // Add sign out logic here
                          console.log('Sign out clicked');
                        }}
                      >
                        Sign out
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Wishlist Link */}
            <Link
              to="/wishlist"
              className={`top-action-link ${isActive('/wishlist') ? 'top-action-link-active' : ''}`}
              data-testid="wishlist-link"
            >
              <HeartIcon />
            </Link>

            {/* Cart Link */}
            <Link
              to="/cart"
              className={`top-action-link ${isActive('/cart') ? 'top-action-link-active' : ''}`}
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
        </div>
      </div>

      {/* Main Navigation Container */}
      <div className="nav-container">
        {/* Mobile Menu Toggle */}
        <button
          className="mobile-menu-toggle"
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          aria-expanded={isMobileMenuOpen}
          aria-label="Toggle mobile menu"
          data-testid="mobile-menu-toggle"
        >
          <MenuIcon />
        </button>

        {/* Categories Navigation (Desktop) / Hidden on Mobile */}
        <nav className="nav-categories" role="navigation" aria-label="Main navigation">
          {categories.map((category) => (
            <button
              key={category.id}
              onClick={() => handleCategoryClick(category)}
              className={`nav-category ${category.id === 'sale' ? 'nav-category-sale' : ''} ${isActive(`/products?category=${category.id}`) ? 'nav-category-active' : ''}`}
              data-testid={`category-${category.id}`}
            >
              {category.name}
            </button>
          ))}
        </nav>

        {/* Brand Logo (Center) */}
        <div className="nav-brand">
          <Link to="/" className="nav-brand-logo" data-testid="nav-brand-logo">
            <span className="brand-text">ModernStore</span>
          </Link>
        </div>

        {/* Search Container (Desktop Right / Mobile Full Width Below) */}
        <div className="nav-search">
          <div className={`search-container ${isSearchFocused ? 'search-focused' : ''}`}>
            <form onSubmit={handleSearchSubmit} className="search-form">
              <div className="search-input-wrapper">
                <div className="search-icon-container">
                  <SearchIcon className="search-icon" />
                </div>
                <input
                  ref={searchRef}
                  type="text"
                  placeholder="Search"
                  value={searchQuery}
                  onChange={handleSearchChange}
                  onFocus={() => setIsSearchFocused(true)}
                  onBlur={() => {
                    setTimeout(() => setIsSearchFocused(false), 200);
                  }}
                  className="search-input"
                  data-testid="search-input"
                  role="combobox"
                  aria-label="Search products"
                  aria-haspopup="listbox"
                  aria-expanded={isSearchFocused && (searchSuggestions.length > 0 || searchQuery.length > 2)}
                  autoComplete="off"
                  spellCheck="false"
                />
                {searchQuery && (
                  <button
                    type="button"
                    className="search-clear-button"
                    onClick={() => {
                      setSearchQuery('');
                      setSearchSuggestions([]);
                      searchRef.current?.focus();
                    }}
                    aria-label="Clear search"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      width="1em"
                      height="1em"
                      fill="currentColor"
                      className="clear-icon"
                    >
                      <path d="M18.3 5.71a.996.996 0 0 0-1.41 0L12 10.59 7.11 5.7A.996.996 0 1 0 5.7 7.11L10.59 12 5.7 16.89a.996.996 0 1 0 1.41 1.41L12 13.41l4.89 4.89a.996.996 0 0 0 1.41-1.41L13.41 12l4.89-4.89c.38-.38.38-1.02 0-1.4z"/>
                    </svg>
                  </button>
                )}
              </div>
              
              {/* Search Suggestions */}
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
              {/* Mobile Categories */}
              <nav className="mobile-nav-categories" role="navigation" aria-label="Mobile navigation">
                {categories.map((category) => (
                  <button
                    key={category.id}
                    onClick={() => handleCategoryClick(category)}
                    className={`mobile-nav-category ${category.id === 'sale' ? 'mobile-nav-category-sale' : ''} ${isActive(`/products?category=${category.id}`) ? 'mobile-nav-category-active' : ''}`}
                    data-testid={`mobile-category-${category.id}`}
                  >
                    {category.name}
                  </button>
                ))}
              </nav>

              {/* Mobile User Actions */}
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
