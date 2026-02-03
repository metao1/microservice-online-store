/**
 * Navigation System Types
 * Enhanced types for professional e-commerce navigation with gender categories,
 * advanced search, Plus membership, and comprehensive mobile support
 */

import { User, Category } from './index';

// Enhanced User interface for Plus membership
export interface EnhancedUser extends User {
  isPlusMember: boolean;
  preferences: UserPreferences;
  membershipTier?: 'plus' | 'premium';
  memberSince?: string;
}

export interface UserPreferences {
  language: string;
  currency: string;
  preferredGender: 'women' | 'men' | 'kids' | null;
  sizePreferences: SizePreference[];
  favoriteCategories: string[];
  notifications: NotificationSettings;
}

export interface SizePreference {
  category: string; // 'clothing', 'shoes', etc.
  size: string;
  fit: 'tight' | 'regular' | 'loose';
  verified: boolean;
}

export interface NotificationSettings {
  email: boolean;
  push: boolean;
  sms: boolean;
  marketing: boolean;
  orderUpdates: boolean;
  priceAlerts: boolean;
  restockAlerts: boolean;
}

// Navigation State Management
export interface NavigationState {
  activeGender: 'women' | 'men' | 'kids' | null;
  activeCategory: string | null;
  isMobileMenuOpen: boolean;
  isSearchFocused: boolean;
  searchQuery: string;
  recentSearches: string[];
  language: string;
  isAccountDropdownOpen: boolean;
  isLanguageDropdownOpen: boolean;
}

// Search System Types
export interface SearchInboxState {
  isOpen: boolean;
  suggestions: SearchSuggestion[];
  recentSearches: string[];
  trendingItems: TrendingItem[];
  popularCategories: Category[];
  isLoading: boolean;
  hasError: boolean;
  errorMessage?: string;
}

export interface SearchSuggestion {
  id: string;
  type: 'product' | 'category' | 'brand' | 'query' | 'trending';
  text: string;
  category?: string;
  gender?: 'women' | 'men' | 'kids';
  imageUrl?: string;
  price?: number;
  originalPrice?: number;
  matchedText: string;
  popularity: number;
  isSponsored?: boolean;
}

export interface TrendingItem {
  id: string;
  title: string;
  category: string;
  gender?: 'women' | 'men' | 'kids';
  searchCount: number;
  trend: 'up' | 'down' | 'stable';
  imageUrl?: string;
}

// Gender-based Navigation
export interface GenderCategory extends Category {
  subcategories: SubCategory[];
  featured: FeaturedItem[];
  imageUrl?: string;
  gender: 'women' | 'men' | 'kids';
}

export interface SubCategory {
  id: string;
  name: string;
  items: CategoryItem[];
  imageUrl?: string;
  parentGender: 'women' | 'men' | 'kids';
  sortOrder: number;
}

export interface CategoryItem {
  id: string;
  name: string;
  path: string;
  imageUrl?: string;
  isPopular?: boolean;
  isNew?: boolean;
  productCount?: number;
}

export interface FeaturedItem {
  id: string;
  title: string;
  imageUrl: string;
  path: string;
  category: string;
}

// Language and Localization
export interface Language {
  code: string; // ISO 639-1 code (e.g., 'en', 'de', 'fr')
  name: string; // English name
  nativeName: string; // Native name
  flag: string; // Flag emoji or URL
  rtl: boolean; // Right-to-left language
  enabled: boolean;
  region?: string; // Optional region code
  currency?: string; // Default currency for this language
}

// Search History and Analytics
export interface SearchHistory {
  userId?: string;
  searches: SearchEntry[];
  maxEntries: number;
  lastCleanup: Date;
}

export interface SearchEntry {
  query: string;
  timestamp: Date;
  resultCount: number;
  clicked: boolean;
  category?: string;
  gender?: string;
}

// Configuration Types
export interface NavigationConfig {
  genderCategories: GenderCategory[];
  languages: Language[];
  searchConfig: SearchConfig;
  mobileConfig: MobileConfig;
  plusMembershipConfig: PlusMembershipConfig;
}

export interface SearchConfig {
  maxSuggestions: number;
  maxRecentSearches: number;
  debounceMs: number;
  minQueryLength: number;
  enableTrending: boolean;
  enableAnalytics: boolean;
  cacheTimeout: number;
}

export interface MobileConfig {
  breakpoint: number; // px
  enableSwipeGestures: boolean;
  animationDuration: number; // ms
  touchOptimization: boolean;
}

export interface PlusMembershipConfig {
  badgeText: string;
  badgeColor: string;
  exclusiveFeatures: string[];
  upgradePrompts: UpgradePrompt[];
}

export interface UpgradePrompt {
  id: string;
  title: string;
  description: string;
  ctaText: string;
  ctaUrl: string;
  priority: number;
}