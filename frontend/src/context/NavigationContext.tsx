/**
 * Navigation Context Provider
 * Manages navigation state, gender selection, mobile menu, language preferences,
 * and user account dropdown for the enhanced navigation system
 */

import { createContext, useContext, ReactNode, useState, useCallback, useEffect } from 'react';
import { NavigationState, GenderCategory, Language, NavigationConfig, EnhancedUser } from '../types/navigation';

interface NavigationContextType {
  navigationState: NavigationState;
  genderCategories: GenderCategory[];
  languages: Language[];
  currentUser: EnhancedUser | null;
  setActiveGender: (gender: 'women' | 'men' | 'kids' | null) => void;
  setActiveCategory: (categoryId: string | null) => void;
  toggleMobileMenu: (open?: boolean) => void;
  setSearchFocus: (focused: boolean) => void;
  updateSearchQuery: (query: string) => void;
  setLanguage: (languageCode: string) => void;
  toggleAccountDropdown: (open?: boolean) => void;
  toggleLanguageDropdown: (open?: boolean) => void;
  addRecentSearch: (query: string) => void;
  clearRecentSearches: () => void;
  updateUserPreferences: (preferences: Partial<EnhancedUser['preferences']>) => void;
}

const NavigationContext = createContext<NavigationContextType | undefined>(undefined);

interface NavigationProviderProps {
  children: ReactNode;
  initialUser?: EnhancedUser | null;
  config?: Partial<NavigationConfig>;
}

const STORAGE_KEYS = {
  NAVIGATION_PREFERENCES: 'navigation_preferences',
  RECENT_SEARCHES: 'navigation_recent_searches',
  SELECTED_LANGUAGE: 'navigation_selected_language',
  SELECTED_GENDER: 'navigation_selected_gender',
};

const DEFAULT_NAVIGATION_STATE: NavigationState = {
  activeGender: null,
  activeCategory: null,
  isMobileMenuOpen: false,
  isSearchFocused: false,
  searchQuery: '',
  recentSearches: [],
  language: 'en',
  isAccountDropdownOpen: false,
  isLanguageDropdownOpen: false,
};

const DEFAULT_GENDER_CATEGORIES: GenderCategory[] = [
  {
    id: 'women',
    name: 'Women',
    gender: 'women',
    subcategories: [
      {
        id: 'women-clothing',
        name: 'Clothing',
        parentGender: 'women',
        sortOrder: 1,
        items: [
          { id: 'dresses', name: 'Dresses', path: '/women/clothing/dresses', isPopular: true },
          { id: 'tops', name: 'Tops', path: '/women/clothing/tops' },
          { id: 'jeans', name: 'Jeans', path: '/women/clothing/jeans', isPopular: true },
          { id: 'jackets', name: 'Jackets', path: '/women/clothing/jackets' },
        ],
      },
      {
        id: 'women-shoes',
        name: 'Shoes',
        parentGender: 'women',
        sortOrder: 2,
        items: [
          { id: 'heels', name: 'Heels', path: '/women/shoes/heels' },
          { id: 'sneakers', name: 'Sneakers', path: '/women/shoes/sneakers', isPopular: true },
          { id: 'boots', name: 'Boots', path: '/women/shoes/boots' },
          { id: 'flats', name: 'Flats', path: '/women/shoes/flats' },
        ],
      },
      {
        id: 'women-accessories',
        name: 'Accessories',
        parentGender: 'women',
        sortOrder: 3,
        items: [
          { id: 'bags', name: 'Bags', path: '/women/accessories/bags', isPopular: true },
          { id: 'jewelry', name: 'Jewelry', path: '/women/accessories/jewelry' },
          { id: 'watches', name: 'Watches', path: '/women/accessories/watches' },
        ],
      },
    ],
    featured: [
      {
        id: 'summer-collection',
        title: 'Summer Collection',
        imageUrl: '/images/women-summer.jpg',
        path: '/women/summer',
        category: 'seasonal',
      },
    ],
  },
  {
    id: 'men',
    name: 'Men',
    gender: 'men',
    subcategories: [
      {
        id: 'men-clothing',
        name: 'Clothing',
        parentGender: 'men',
        sortOrder: 1,
        items: [
          { id: 'shirts', name: 'Shirts', path: '/men/clothing/shirts', isPopular: true },
          { id: 'jeans', name: 'Jeans', path: '/men/clothing/jeans', isPopular: true },
          { id: 'jackets', name: 'Jackets', path: '/men/clothing/jackets' },
          { id: 'suits', name: 'Suits', path: '/men/clothing/suits' },
        ],
      },
      {
        id: 'men-shoes',
        name: 'Shoes',
        parentGender: 'men',
        sortOrder: 2,
        items: [
          { id: 'sneakers', name: 'Sneakers', path: '/men/shoes/sneakers', isPopular: true },
          { id: 'dress-shoes', name: 'Dress Shoes', path: '/men/shoes/dress' },
          { id: 'boots', name: 'Boots', path: '/men/shoes/boots' },
        ],
      },
      {
        id: 'men-accessories',
        name: 'Accessories',
        parentGender: 'men',
        sortOrder: 3,
        items: [
          { id: 'watches', name: 'Watches', path: '/men/accessories/watches', isPopular: true },
          { id: 'bags', name: 'Bags', path: '/men/accessories/bags' },
          { id: 'belts', name: 'Belts', path: '/men/accessories/belts' },
        ],
      },
    ],
    featured: [
      {
        id: 'business-collection',
        title: 'Business Collection',
        imageUrl: '/images/men-business.jpg',
        path: '/men/business',
        category: 'professional',
      },
    ],
  },
  {
    id: 'kids',
    name: 'Kids',
    gender: 'kids',
    subcategories: [
      {
        id: 'kids-clothing',
        name: 'Clothing',
        parentGender: 'kids',
        sortOrder: 1,
        items: [
          { id: 'tops', name: 'Tops', path: '/kids/clothing/tops' },
          { id: 'bottoms', name: 'Bottoms', path: '/kids/clothing/bottoms' },
          { id: 'dresses', name: 'Dresses', path: '/kids/clothing/dresses' },
        ],
      },
      {
        id: 'kids-shoes',
        name: 'Shoes',
        parentGender: 'kids',
        sortOrder: 2,
        items: [
          { id: 'sneakers', name: 'Sneakers', path: '/kids/shoes/sneakers', isPopular: true },
          { id: 'sandals', name: 'Sandals', path: '/kids/shoes/sandals' },
        ],
      },
    ],
    featured: [
      {
        id: 'back-to-school',
        title: 'Back to School',
        imageUrl: '/images/kids-school.jpg',
        path: '/kids/school',
        category: 'seasonal',
      },
    ],
  },
];

const DEFAULT_LANGUAGES: Language[] = [
  {
    code: 'en',
    name: 'English',
    nativeName: 'English',
    flag: '🇺🇸',
    rtl: false,
    enabled: true,
    currency: 'USD',
  },
  {
    code: 'de',
    name: 'German',
    nativeName: 'Deutsch',
    flag: '🇩🇪',
    rtl: false,
    enabled: true,
    currency: 'EUR',
  },
  {
    code: 'fr',
    name: 'French',
    nativeName: 'Français',
    flag: '🇫🇷',
    rtl: false,
    enabled: true,
    currency: 'EUR',
  },
  {
    code: 'es',
    name: 'Spanish',
    nativeName: 'Español',
    flag: '🇪🇸',
    rtl: false,
    enabled: true,
    currency: 'EUR',
  },
];

export const NavigationProvider: React.FC<NavigationProviderProps> = ({ 
  children, 
  initialUser = null,
  config = {} 
}) => {
  const [navigationState, setNavigationState] = useState<NavigationState>(DEFAULT_NAVIGATION_STATE);
  const [currentUser, setCurrentUser] = useState<EnhancedUser | null>(initialUser);
  const [genderCategories] = useState<GenderCategory[]>(config.genderCategories || DEFAULT_GENDER_CATEGORIES);
  const [languages] = useState<Language[]>(config.languages || DEFAULT_LANGUAGES);

  // Load preferences from localStorage on mount
  useEffect(() => {
    try {
      const storedLanguage = localStorage.getItem(STORAGE_KEYS.SELECTED_LANGUAGE);
      const storedGender = localStorage.getItem(STORAGE_KEYS.SELECTED_GENDER);
      const storedRecentSearches = localStorage.getItem(STORAGE_KEYS.RECENT_SEARCHES);

      setNavigationState(prev => ({
        ...prev,
        language: storedLanguage || 'en',
        activeGender: (storedGender as 'women' | 'men' | 'kids') || null,
        recentSearches: storedRecentSearches ? JSON.parse(storedRecentSearches) : [],
      }));
    } catch (error) {
      console.error('Failed to load navigation preferences from localStorage:', error);
    }
  }, []);

  // Save preferences to localStorage when they change
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.SELECTED_LANGUAGE, navigationState.language);
      if (navigationState.activeGender) {
        localStorage.setItem(STORAGE_KEYS.SELECTED_GENDER, navigationState.activeGender);
      }
      localStorage.setItem(STORAGE_KEYS.RECENT_SEARCHES, JSON.stringify(navigationState.recentSearches));
    } catch (error) {
      console.error('Failed to save navigation preferences to localStorage:', error);
    }
  }, [navigationState.language, navigationState.activeGender, navigationState.recentSearches]);

  const setActiveGender = useCallback((gender: 'women' | 'men' | 'kids' | null) => {
    setNavigationState(prev => ({
      ...prev,
      activeGender: gender,
      activeCategory: null, // Reset category when gender changes
    }));
  }, []);

  const setActiveCategory = useCallback((categoryId: string | null) => {
    setNavigationState(prev => ({
      ...prev,
      activeCategory: categoryId,
    }));
  }, []);

  const toggleMobileMenu = useCallback((open?: boolean) => {
    setNavigationState(prev => ({
      ...prev,
      isMobileMenuOpen: open !== undefined ? open : !prev.isMobileMenuOpen,
      // Close other dropdowns when mobile menu opens
      isAccountDropdownOpen: open ? false : prev.isAccountDropdownOpen,
      isLanguageDropdownOpen: open ? false : prev.isLanguageDropdownOpen,
    }));
  }, []);

  const setSearchFocus = useCallback((focused: boolean) => {
    setNavigationState(prev => ({
      ...prev,
      isSearchFocused: focused,
    }));
  }, []);

  const updateSearchQuery = useCallback((query: string) => {
    setNavigationState(prev => ({
      ...prev,
      searchQuery: query,
    }));
  }, []);

  const setLanguage = useCallback((languageCode: string) => {
    const language = languages.find(lang => lang.code === languageCode);
    if (language) {
      setNavigationState(prev => ({
        ...prev,
        language: languageCode,
        isLanguageDropdownOpen: false,
      }));

      // Update user preferences if user is logged in
      if (currentUser) {
        setCurrentUser(prev => prev ? {
          ...prev,
          preferences: {
            ...prev.preferences,
            language: languageCode,
            currency: language.currency || prev.preferences.currency,
          },
        } : null);
      }
    }
  }, [languages, currentUser]);

  const toggleAccountDropdown = useCallback((open?: boolean) => {
    setNavigationState(prev => ({
      ...prev,
      isAccountDropdownOpen: open !== undefined ? open : !prev.isAccountDropdownOpen,
      // Close other dropdowns
      isLanguageDropdownOpen: false,
    }));
  }, []);

  const toggleLanguageDropdown = useCallback((open?: boolean) => {
    setNavigationState(prev => ({
      ...prev,
      isLanguageDropdownOpen: open !== undefined ? open : !prev.isLanguageDropdownOpen,
      // Close other dropdowns
      isAccountDropdownOpen: false,
    }));
  }, []);

  const addRecentSearch = useCallback((query: string) => {
    if (!query.trim()) return;

    setNavigationState(prev => {
      const filteredSearches = prev.recentSearches.filter(search => search !== query.trim());
      const newRecentSearches = [query.trim(), ...filteredSearches].slice(0, 10);
      
      return {
        ...prev,
        recentSearches: newRecentSearches,
      };
    });
  }, []);

  const clearRecentSearches = useCallback(() => {
    setNavigationState(prev => ({
      ...prev,
      recentSearches: [],
    }));
    
    try {
      localStorage.removeItem(STORAGE_KEYS.RECENT_SEARCHES);
    } catch (error) {
      console.error('Failed to clear recent searches from localStorage:', error);
    }
  }, []);

  const updateUserPreferences = useCallback((preferences: Partial<EnhancedUser['preferences']>) => {
    if (currentUser) {
      setCurrentUser(prev => prev ? {
        ...prev,
        preferences: {
          ...prev.preferences,
          ...preferences,
        },
      } : null);
    }
  }, [currentUser]);

  const value: NavigationContextType = {
    navigationState,
    genderCategories,
    languages,
    currentUser,
    setActiveGender,
    setActiveCategory,
    toggleMobileMenu,
    setSearchFocus,
    updateSearchQuery,
    setLanguage,
    toggleAccountDropdown,
    toggleLanguageDropdown,
    addRecentSearch,
    clearRecentSearches,
    updateUserPreferences,
  };

  return (
    <NavigationContext.Provider value={value}>
      {children}
    </NavigationContext.Provider>
  );
};

export const useNavigationContext = () => {
  const context = useContext(NavigationContext);
  if (!context) {
    throw new Error('useNavigationContext must be used within NavigationProvider');
  }
  return context;
};