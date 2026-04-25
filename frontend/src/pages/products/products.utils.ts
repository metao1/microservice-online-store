import { Product } from '@types';
import { CATEGORY_SEGMENT_PRESETS } from './products.config';
import { ProductSortBy, ProductSortOrder, SegmentTab, SelectedFilters } from './products.types';

const hashSku = (sku: string) => {
  let hash = 0;
  for (let i = 0; i < sku.length; i += 1) {
    hash = (hash * 31 + sku.charCodeAt(i)) >>> 0;
  }
  return hash;
};

const getFacet = (product: Product) => {
  const seed = hashSku(product.sku);
  const material = ['leather', 'canvas', 'synthetic'][seed % 3];
  const heel = ['flat', 'block'][seed % 2];
  const shoeWidth = ['narrow', 'regular', 'wide'][seed % 3];
  const toe = ['round', 'pointed'][seed % 2];
  const sustainable = seed % 7 === 0;
  const premium = (product.isFeatured ?? false) || product.price >= 100 || seed % 5 === 0;

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

export const formatCategoryLabel = (category: string) =>
  category
    .split(/[-_\s]+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');

export const buildSegmentTabsForCategory = (categoryName: string): SegmentTab[] => {
  const normalized = categoryName.trim().toLowerCase();
  if (normalized in CATEGORY_SEGMENT_PRESETS) {
    return CATEGORY_SEGMENT_PRESETS[normalized];
  }
  return [
    { id: `${normalized || 'general'}-popular`, name: 'Popular', terms: ['popular', 'featured', 'top'] },
    { id: `${normalized || 'general'}-new`, name: 'New', terms: ['new', 'latest', 'recent'] },
    { id: `${normalized || 'general'}-essentials`, name: 'Essentials', terms: ['essential', 'classic', 'core'] },
  ];
};

export const getProductSearchText = (product: Product): string =>
  [
    product.title,
    product.description,
    product.brand,
    product.category,
    ...(product.categories || []),
    ...(product.tags || []),
  ]
    .filter(Boolean)
    .join(' ')
    .toLowerCase();

export const buildCategoryTabs = (availableCategories: string[], activeCategory: string) => {
  const known = Object.keys(CATEGORY_SEGMENT_PRESETS);
  const seed = [...availableCategories, ...known, activeCategory].filter(Boolean);
  const uniqueNormalized = Array.from(new Set(seed.map((category) => category.trim().toLowerCase()).filter(Boolean)));

  return uniqueNormalized.map((category) => ({
    id: category,
    label: formatCategoryLabel(category),
  }));
};

export const sortProducts = (products: Product[], sortBy: ProductSortBy, sortOrder: ProductSortOrder): Product[] => {
  return [...products].sort((left, right) => {
    let leftValue: string | number = '';
    let rightValue: string | number = '';

    switch (sortBy) {
      case 'name':
        leftValue = left.title.toLowerCase();
        rightValue = right.title.toLowerCase();
        break;
      case 'price':
        leftValue = left.price;
        rightValue = right.price;
        break;
      case 'rating':
        leftValue = left.rating || 0;
        rightValue = right.rating || 0;
        break;
    }

    if (sortOrder === 'asc') {
      return leftValue < rightValue ? -1 : leftValue > rightValue ? 1 : 0;
    }
    return leftValue > rightValue ? -1 : leftValue < rightValue ? 1 : 0;
  });
};

export const filterProducts = (products: Product[], selectedFilters: SelectedFilters): Product[] => {
  return products.filter((product) => {
    const facet = getFacet(product);

    if (selectedFilters.brand) {
      const brand = (product.brand || '').toLowerCase();
      if (brand !== selectedFilters.brand.toLowerCase()) return false;
    }

    if (selectedFilters.size) {
      const hasSize = (product.variants || []).some((variant) => variant.type === 'size' && variant.value === selectedFilters.size);
      if (!hasSize) return false;
    }

    if (selectedFilters.color) {
      const wanted = selectedFilters.color.toLowerCase();
      const aliases = wanted === 'blue' ? new Set(['blue', 'navy']) : new Set([wanted]);
      const hasColor = (product.variants || []).some(
        (variant) => variant.type === 'color' && aliases.has(variant.name.toLowerCase())
      );
      if (!hasColor) return false;
    }

    if (selectedFilters.price) {
      const range = parsePriceRange(selectedFilters.price);
      if (range) {
        if (product.price < range.min) return false;
        if (range.max != null && product.price > range.max) return false;
      }
    }

    if (selectedFilters.collection) {
      if (selectedFilters.collection === 'new' && !product.isNew) return false;
      if (selectedFilters.collection === 'sale' && !product.isSale) return false;
    }

    if (selectedFilters.material && facet.material !== selectedFilters.material) return false;
    if (selectedFilters.heel && facet.heel !== selectedFilters.heel) return false;
    if (selectedFilters.shoeWidth && facet.shoeWidth !== selectedFilters.shoeWidth) return false;
    if (selectedFilters.toe && facet.toe !== selectedFilters.toe) return false;

    if (selectedFilters.qualities) {
      if (selectedFilters.qualities === 'premium' && !facet.premium) return false;
      if (selectedFilters.qualities === 'sustainable' && !facet.sustainable) return false;
    }

    return true;
  });
};

export const applySegmentFilter = (
  products: Product[],
  segmentTabs: SegmentTab[],
  activeSegment: string,
  toSearchText: (product: Product) => string
): Product[] => {
  const activeTab = segmentTabs.find((tab) => tab.id === activeSegment);
  if (!activeTab) {
    return products;
  }

  const segmentMatches = products.filter((product) => {
    const text = toSearchText(product);
    return activeTab.terms.some((term) => text.includes(term));
  });

  return segmentMatches.length > 0 ? segmentMatches : products;
};

