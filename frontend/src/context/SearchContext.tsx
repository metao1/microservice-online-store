/**
 * Search Context Provider
 * Manages search state, suggestions, history, and trending data
 * for the enhanced navigation system
 */

import { createContext, useContext, ReactNode, useState, useCallback, useEffect } from 'react';
import { SearchInboxState, SearchSuggestion, TrendingItem, SearchHistory, SearchEntry } from '../types/navigation';
import { Category } from '../types';

interface SearchContextType {
  searchState: SearchInboxState;
  searchHistory: SearchHistory;
  updateSearchQuery: (query: string) => void;
  addToSearchHistory: (entry: SearchEntry) => void;
  clearSearchHistory: () => void;
  getSuggestions: (query: string) => Promise<SearchSuggestion[]>;
  getTrendingItems: () => Promise<TrendingItem[]>;
  getPopularCategories: () => Promise<Category[]>;
  setSearchFocus: (focused: boolean) => void;
  clearSearch: () => void;
}

const SearchContext = createContext<SearchContextType | undefined>(undefined);

interface SearchProviderProps {
  children: ReactNode;
  userId?: string;
}

const STORAGE_KEYS = {
  SEARCH_HISTORY: 'navigation_search_history',
  TRENDING_CACHE: 'navigation_trending_cache',
  POPULAR_CATEGORIES_CACHE: 'navigation_popular_categories_cache',
};

const DEFAULT_SEARCH_STATE: SearchInboxState = {
  isOpen: false,
  suggestions: [],
  recentSearches: [],
  trendingItems: [],
  popularCategories: [],
  isLoading: false,
  hasError: false,
};

const DEFAULT_SEARCH_HISTORY: SearchHistory = {
  searches: [],
  maxEntries: 10,
  lastCleanup: new Date(),
};

export const SearchProvider: React.FC<SearchProviderProps> = ({ children, userId }) => {
  const [searchState, setSearchState] = useState<SearchInboxState>(DEFAULT_SEARCH_STATE);
  const [searchHistory, setSearchHistory] = useState<SearchHistory>(DEFAULT_SEARCH_HISTORY);

  // Load search history from localStorage on mount
  useEffect(() => {
    try {
      const storedHistory = localStorage.getItem(STORAGE_KEYS.SEARCH_HISTORY);
      if (storedHistory) {
        const parsed = JSON.parse(storedHistory);
        setSearchHistory({
          ...parsed,
          lastCleanup: new Date(parsed.lastCleanup),
          searches: parsed.searches.map((search: any) => ({
            ...search,
            timestamp: new Date(search.timestamp),
          })),
        });
        
        // Update recent searches in search state
        setSearchState(prev => ({
          ...prev,
          recentSearches: parsed.searches.slice(0, 5).map((search: SearchEntry) => search.query),
        }));
      }
    } catch (error) {
      console.error('Failed to load search history from localStorage:', error);
    }
  }, []);

  // Save search history to localStorage when it changes
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.SEARCH_HISTORY, JSON.stringify(searchHistory));
    } catch (error) {
      console.error('Failed to save search history to localStorage:', error);
    }
  }, [searchHistory]);

  const updateSearchQuery = useCallback((query: string) => {
    setSearchState(prev => ({
      ...prev,
      isOpen: query.length > 0,
    }));
  }, []);

  const addToSearchHistory = useCallback((entry: SearchEntry) => {
    setSearchHistory(prev => {
      // Remove duplicate entries
      const filteredSearches = prev.searches.filter(search => search.query !== entry.query);
      
      // Add new entry at the beginning
      const newSearches = [entry, ...filteredSearches].slice(0, prev.maxEntries);
      
      const newHistory = {
        ...prev,
        searches: newSearches,
      };

      // Update recent searches in search state
      setSearchState(prevState => ({
        ...prevState,
        recentSearches: newSearches.slice(0, 5).map(search => search.query),
      }));

      return newHistory;
    });
  }, []);

  const clearSearchHistory = useCallback(() => {
    setSearchHistory(DEFAULT_SEARCH_HISTORY);
    setSearchState(prev => ({
      ...prev,
      recentSearches: [],
    }));
    
    try {
      localStorage.removeItem(STORAGE_KEYS.SEARCH_HISTORY);
    } catch (error) {
      console.error('Failed to clear search history from localStorage:', error);
    }
  }, []);

  const getSuggestions = useCallback(async (query: string): Promise<SearchSuggestion[]> => {
    if (query.length < 2) return [];

    setSearchState(prev => ({ ...prev, isLoading: true, hasError: false }));

    try {
      // Mock API call - replace with actual API integration
      await new Promise(resolve => setTimeout(resolve, 200)); // Simulate network delay
      
      const mockSuggestions: SearchSuggestion[] = [
        {
          id: '1',
          type: 'product' as const,
          text: `${query} dress`,
          category: 'clothing',
          gender: 'women' as const,
          matchedText: query,
          popularity: 95,
        },
        {
          id: '2',
          type: 'category' as const,
          text: `${query} shoes`,
          category: 'shoes',
          matchedText: query,
          popularity: 87,
        },
        {
          id: '3',
          type: 'brand' as const,
          text: `${query} brand`,
          matchedText: query,
          popularity: 76,
        },
      ].filter(suggestion => 
        suggestion.text.toLowerCase().includes(query.toLowerCase())
      );

      setSearchState(prev => ({
        ...prev,
        suggestions: mockSuggestions,
        isLoading: false,
      }));

      return mockSuggestions;
    } catch (error) {
      console.error('Failed to fetch search suggestions:', error);
      setSearchState(prev => ({
        ...prev,
        isLoading: false,
        hasError: true,
        errorMessage: 'Failed to load suggestions',
      }));
      return [];
    }
  }, []);

  const getTrendingItems = useCallback(async (): Promise<TrendingItem[]> => {
    try {
      // Check cache first
      const cached = localStorage.getItem(STORAGE_KEYS.TRENDING_CACHE);
      if (cached) {
        const { data, timestamp } = JSON.parse(cached);
        const isExpired = Date.now() - timestamp > 30 * 60 * 1000; // 30 minutes
        if (!isExpired) {
          return data;
        }
      }

      // Mock API call - replace with actual API integration
      const mockTrendingItems: TrendingItem[] = [
        {
          id: '1',
          title: 'Summer Dresses',
          category: 'clothing',
          gender: 'women',
          searchCount: 1250,
          trend: 'up',
        },
        {
          id: '2',
          title: 'Sneakers',
          category: 'shoes',
          searchCount: 980,
          trend: 'stable',
        },
        {
          id: '3',
          title: 'Backpacks',
          category: 'accessories',
          searchCount: 756,
          trend: 'up',
        },
      ];

      // Cache the results
      localStorage.setItem(STORAGE_KEYS.TRENDING_CACHE, JSON.stringify({
        data: mockTrendingItems,
        timestamp: Date.now(),
      }));

      setSearchState(prev => ({
        ...prev,
        trendingItems: mockTrendingItems,
      }));

      return mockTrendingItems;
    } catch (error) {
      console.error('Failed to fetch trending items:', error);
      return [];
    }
  }, []);

  const getPopularCategories = useCallback(async (): Promise<Category[]> => {
    try {
      // Check cache first
      const cached = localStorage.getItem(STORAGE_KEYS.POPULAR_CATEGORIES_CACHE);
      if (cached) {
        const { data, timestamp } = JSON.parse(cached);
        const isExpired = Date.now() - timestamp > 60 * 60 * 1000; // 1 hour
        if (!isExpired) {
          return data;
        }
      }

      // Mock API call - replace with actual API integration
      const mockPopularCategories: Category[] = [
        { id: 'clothing', name: 'Clothing' },
        { id: 'shoes', name: 'Shoes' },
        { id: 'accessories', name: 'Accessories' },
        { id: 'beauty', name: 'Beauty' },
      ];

      // Cache the results
      localStorage.setItem(STORAGE_KEYS.POPULAR_CATEGORIES_CACHE, JSON.stringify({
        data: mockPopularCategories,
        timestamp: Date.now(),
      }));

      setSearchState(prev => ({
        ...prev,
        popularCategories: mockPopularCategories,
      }));

      return mockPopularCategories;
    } catch (error) {
      console.error('Failed to fetch popular categories:', error);
      return [];
    }
  }, []);

  const setSearchFocus = useCallback((focused: boolean) => {
    setSearchState(prev => ({
      ...prev,
      isOpen: focused,
    }));
  }, []);

  const clearSearch = useCallback(() => {
    setSearchState(prev => ({
      ...prev,
      isOpen: false,
      suggestions: [],
      isLoading: false,
      hasError: false,
      errorMessage: undefined,
    }));
  }, []);

  const value: SearchContextType = {
    searchState,
    searchHistory,
    updateSearchQuery,
    addToSearchHistory,
    clearSearchHistory,
    getSuggestions,
    getTrendingItems,
    getPopularCategories,
    setSearchFocus,
    clearSearch,
  };

  return (
    <SearchContext.Provider value={value}>
      {children}
    </SearchContext.Provider>
  );
};

export const useSearchContext = () => {
  const context = useContext(SearchContext);
  if (!context) {
    throw new Error('useSearchContext must be used within SearchProvider');
  }
  return context;
};
