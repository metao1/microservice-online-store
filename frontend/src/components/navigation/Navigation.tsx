/**
 * Navigation Component
 * Redesigned to match layout across desktop and mobile.
 */

import {FC, useEffect, useMemo, useRef, useState} from 'react';
import {Link, useLocation, useNavigate} from 'react-router-dom';
import {useCartContext} from '@context/CartContext';
import Badge from '../ui/Badge/Badge';
import {Category} from '@types';
import {apiClient} from '@services/api';
import './Navigation.css';
import {
  BackIcon,
  ChevronDownIcon,
  GlobeIcon,
  HeartIcon,
  MenuIcon,
  SearchIcon,
  ShoppingBagIcon,
  UserIcon
} from './icons';
import SearchSuggestions from './SearchSuggestions';

interface NavigationProps {
  categories?: Category[];
  onSearch?: (query: string) => void;
  onCategorySelect?: (categoryId: string) => void;
}

const Navigation: FC<NavigationProps> = ({
                                           categories,
                                           onSearch,
                                           onCategorySelect,
                                         }) => {
  const {getCartItemCount} = useCartContext();
  const itemCount = getCartItemCount();
  const location = useLocation();
  const navigate = useNavigate();

  const utilityLinks = [
    'Help and contact',
    'Free standard delivery over €29,90 & free returns*',
    '30-day return policy',
    'Gift Cards'
  ];

  const [secondaryCategories, setSecondaryCategories] = useState<Category[]>([]);
  const [isLoadingCategories, setIsLoadingCategories] = useState(false);

  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [searchSuggestions, setSearchSuggestions] = useState<string[]>([]);
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isMobileSearchActive, setIsMobileSearchActive] = useState(false);
  const [activeMegaCategory, setActiveMegaCategory] = useState<string | null>(null);
  const [subcategoryCache, setSubcategoryCache] = useState<Record<string, Category[]>>({});
  const [isLoadingSubcategories, setIsLoadingSubcategories] = useState(false);
  const [isMobileView, setIsMobileView] = useState(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return false;
    return window.matchMedia('(max-width: 900px)').matches;
  });

  // Refs
  const desktopSearchRef = useRef<HTMLInputElement>(null);
  const mobileSearchRef = useRef<HTMLInputElement>(null);

  const isActive = (path: string) => location.pathname === path;

  const megaCategories = useMemo(() => ([
    'Computers & Accessories',
    'Mobile / PDAs & Handhelds',
    'Audio & Video',
    'Car Electronics',
    'GPS & Navigation',
    'Office & Business',
    'Books & Learning',
    'Toys & Kids Electronics',
    'All Electronics / Bundles',
  ]), []);

  const groupedSecondaryCategories = useMemo(() => {
    const groups: Record<string, Category[]> = {};
    const remaining: Category[] = [];
    megaCategories.forEach((key) => {
      groups[key] = [];
    });

    secondaryCategories.forEach((category) => {
      const label = (category.category || category.name || '').toLowerCase();
      const matchedKey = megaCategories.find((key) => {
        const lowerKey = key.toLowerCase();
        if (lowerKey.includes('computers')) {
          return label.includes('computer') || label.includes('laptop') || label.includes('netbook') || label.includes('docking');
        }
        if (lowerKey.includes('mobile / pdas')) {
          return label.includes('pda') || label.includes('handheld') || label.includes('tablet') || label.includes('touch screen') || label.includes('skins') || label.includes('covers');
        }
        if (lowerKey.includes('audio & video')) {
          return label.includes('audio') || label.includes('video') || label.includes('headphone') || label.includes('remote control') || label.includes('tv remote');
        }
        if (lowerKey.includes('car electronics')) {
          return label.includes('car') || label.includes('vehicle') || label.includes('stereo');
        }
        if (lowerKey.includes('gps')) {
          return label.includes('gps') || label.includes('navigation');
        }
        if (lowerKey.includes('office')) {
          return label.includes('office');
        }
        if (lowerKey.includes('books')) {
          return label.includes('book') || label.includes('dictionary') || label.includes('thesauri') || label.includes('charts') || label.includes('maps') || label.includes('education') || label.includes('learning');
        }
        if (lowerKey.includes('toys')) {
          return label.includes('toy') || label.includes('kids');
        }
        if (lowerKey.includes('all electronics')) {
          return label.includes('electronics') || label.includes('bundles');
        }
        return label.includes(lowerKey);
      });
      if (matchedKey) {
        groups[matchedKey].push(category);
      } else {
        remaining.push(category);
      }
    });

    // If a group is empty, fill it with a few remaining categories (round-robin)
    let cursor = 0;
    megaCategories.forEach((key) => {
      if (groups[key].length === 0 && remaining.length > 0) {
        const slice = remaining.slice(cursor, cursor + 6);
        groups[key] = slice;
        cursor += slice.length;
      }
    });

    return {groups, remaining};
  }, [secondaryCategories, megaCategories]);

  useEffect(() => {
    let isMounted = true;
    const loadCategories = async () => {
      setIsLoadingCategories(true);
      try {
        const data = await apiClient.getCategories(50, 0);
        if (isMounted) {
          setSecondaryCategories(data);
        }
      } catch (error) {
        if (isMounted) {
          console.error('Failed to fetch search suggestions:', error);
        }
      } finally {
        if (isMounted) {
          setIsLoadingCategories(false);
        }
      }
      ;
    };

    loadCategories();
    return () => {
      isMounted = false;
    };
  }, []);

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
    const label = category.category || category.name || '';
    onCategorySelect?.(label);
    if (label) {
      navigate(`/products?category=${encodeURIComponent(label)}`);
    }
    setIsMobileMenuOpen(false);
  };

  const handleSearchFocus = (isMobile: boolean) => {
    setIsSearchFocused(true);
    if (isMobile) {
      setIsMobileSearchActive(true);
    }
  };

  const loadSubcategories = async (category: string) => {
    setActiveMegaCategory(category);
    if (subcategoryCache[category]) return;
    setIsLoadingSubcategories(true);
    try {
      const subs = await apiClient.getSubcategories(category, 20, 0);
      setSubcategoryCache((prev) => ({ ...prev, [category]: subs }));
    } catch (error) {
      console.error('Failed to fetch subcategories:', error);
    } finally {
      setIsLoadingSubcategories(false);
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
    if (typeof window === 'undefined' || !window.matchMedia) return;
    const mediaQuery = window.matchMedia('(max-width: 900px)');

    const handleChange = (event: MediaQueryListEvent) => {
      const matches = event.matches;
      setIsMobileView(matches);
      if (!matches) {
        setIsMobileSearchActive(false);
        setIsSearchFocused(false);
        setSearchSuggestions([]);
      }
    };

    setIsMobileView(mediaQuery.matches);
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
                  <GlobeIcon/>
                </button>
                <Link
                    to="/account"
                    className={`icon-link ${isActive('/account') ? 'active' : ''}`}
                    data-testid="account-link"
                >
                  <UserIcon/>
                </Link>
                <Link
                    to="/wishlist"
                    className={`icon-link ${isActive('/wishlist') ? 'active' : ''}`}
                    data-testid="wishlist-link"
                >
                  <HeartIcon/>
                </Link>
                <Link
                    to="/cart"
                    className={`icon-link ${isActive('/cart') ? 'active' : ''}`}
                    data-testid="cart-link"
                >
                  <ShoppingBagIcon/>
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
                    <SearchIcon className="search-icon"/>
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

                  <SearchSuggestions
                      isOpen={isSearchFocused}
                      isLoading={isLoadingSuggestions}
                      suggestions={searchSuggestions}
                      query={searchQuery}
                      onSelect={handleSuggestionClick}
                  />
                </form>
              </div>
            </div>
          </div>

          {/* Secondary Categories */}
          <div
              className="nav-secondary"
              role="navigation"
              aria-label="Secondary navigation"
              onMouseLeave={() => setActiveMegaCategory(null)}
          >
            {isLoadingCategories && secondaryCategories.length === 0 ? (
                <span className="nav-secondary-loading">Loading...</span>
            ) : (
                megaCategories.map((category) => (
                    <button
                        key={category}
                        className={`nav-secondary-link ${/sale/i.test(category) ? 'sale' : ''}`}
                        onMouseEnter={() => loadSubcategories(category)}
                        onFocus={() => loadSubcategories(category)}
                    >
                      {category}
                    </button>
                ))
            )}

            {activeMegaCategory && (
                <div className="nav-mega-panel" role="dialog" aria-label={`${activeMegaCategory} categories`}>
                  {isLoadingSubcategories && !subcategoryCache[activeMegaCategory] && (
                      <div className="nav-mega-loading">Loading categories...</div>
                  )}
                  {(() => {
                    const items = subcategoryCache[activeMegaCategory] || groupedSecondaryCategories.groups[activeMegaCategory] || [];
                    const primary = items.slice(0, 10);
                    const more = items.slice(10, 20);
                    return (
                  <div className="nav-mega-columns">
                    <div className="nav-mega-column">
                      <h4>Categories</h4>
                      <ul>
                        {primary.slice(0, 6).map((item) => {
                          const label = item.category || item.name || '';
                          return (
                              <li key={label}>
                                <button onClick={() => handleCategoryClick({category: label})}>
                                  {label}
                                </button>
                              </li>
                          );
                        })}
                      </ul>
                    </div>
                    <div className="nav-mega-column">
                      <h4>More categories</h4>
                      <ul>
                        {(more.length ? more : primary.slice(6, 12)).map((item) => {
                          const label = item.category || item.name || '';
                          return (
                              <li key={label}>
                                <button onClick={() => handleCategoryClick({category: label})}>
                                  {label}
                                </button>
                              </li>
                          );
                        })}
                      </ul>
                    </div>
                    <div className="nav-mega-column">
                      <h4>Highlights</h4>
                      <ul>
                        <li>
                          <button onClick={() => handleCategoryClick({category: 'New arrivals'})}>New arrivals</button>
                        </li>
                        <li>
                          <button onClick={() => handleCategoryClick({category: 'Latest sneaker'})}>Latest sneaker
                          </button>
                        </li>
                        <li>
                          <button onClick={() => handleCategoryClick({category: 'Gift cards'})}>Gift cards</button>
                        </li>
                      </ul>
                    </div>
                    <div className="nav-mega-visual">
                      <div className="nav-mega-card">
                        <span className="nav-mega-card-title">Dreamly cozy</span>
                        <span className="nav-mega-card-cta">Shop now →</span>
                      </div>
                    </div>
                  </div>
                    );
                  })()}
                </div>
            )}
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
                  <MenuIcon/>
                </button>

                <div className="nav-search nav-search-mobile">
                  <form onSubmit={handleSearchSubmit} className="search-form">
                    <div className="search-input-wrapper">
                      <SearchIcon className="search-icon"/>
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
                  <BackIcon/>
                </button>
                <button type="button" className="mobile-search-all">
                  <span>All</span>
                  <ChevronDownIcon/>
                </button>
                <div className="mobile-search-input">
                  <SearchIcon className="search-icon"/>
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
                    {(categories || []).map((category) => {
                      const label = category.category || category.name || '';
                      return (
                          <button
                              key={label}
                              onClick={() => handleCategoryClick(category)}
                              className={`mobile-nav-category ${isActive(`/products?category=${encodeURIComponent(label)}`) ? 'mobile-nav-category-active' : ''}`}
                              data-testid={`mobile-category-${encodeURIComponent(label)}`}
                          >
                            {label}
                          </button>
                      );
                    })}
                  </nav>

                  <div className="mobile-user-actions">
                    <Link
                        to="/account"
                        className={`mobile-user-link ${isActive('/account') ? 'mobile-user-link-active' : ''}`}
                        onClick={() => setIsMobileMenuOpen(false)}
                    >
                      <UserIcon/>
                      Account
                    </Link>
                    <Link
                        to="/wishlist"
                        className={`mobile-user-link ${isActive('/wishlist') ? 'mobile-user-link-active' : ''}`}
                        onClick={() => setIsMobileMenuOpen(false)}
                    >
                      <HeartIcon/>
                      Wishlist
                    </Link>
                    <Link
                        to="/cart"
                        className={`mobile-user-link ${isActive('/cart') ? 'mobile-user-link-active' : ''}`}
                        onClick={() => setIsMobileMenuOpen(false)}
                    >
                      <ShoppingBagIcon/>
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
