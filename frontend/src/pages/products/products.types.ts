export type ProductSortBy = 'name' | 'price' | 'rating';

export type ProductSortOrder = 'asc' | 'desc';

export interface SegmentTab {
  id: string;
  name: string;
  terms: string[];
}

export interface FilterOption {
  value: string;
  label: string;
}

export interface FilterGroup {
  id: keyof SelectedFilters;
  label: string;
  options: FilterOption[];
}

export interface SelectedFilters {
  size: string;
  brand: string;
  price: string;
  color: string;
  qualities: string;
  collection: string;
  material: string;
  heel: string;
  shoeWidth: string;
  toe: string;
}

