import { FilterGroup, SegmentTab, SelectedFilters } from './products.types';

export const CATEGORY_SEGMENT_PRESETS: Record<string, SegmentTab[]> = {
  books: [
    {
      id: 'technology',
      name: 'Technology',
      terms: ['software', 'programming', 'code', 'architecture', 'domain', 'design', 'tech', 'computer', 'data'],
    },
    {
      id: 'business',
      name: 'Business',
      terms: ['business', 'startup', 'leadership', 'management', 'finance', 'marketing', 'strategy'],
    },
    { id: 'fiction', name: 'Fiction', terms: ['fiction', 'novel', 'story', 'fantasy', 'romance', 'mystery', 'thriller'] },
  ],
  electronics: [
    { id: 'audio', name: 'Audio', terms: ['audio', 'headphone', 'speaker', 'earbud', 'sound'] },
    { id: 'computing', name: 'Computing', terms: ['laptop', 'computer', 'keyboard', 'monitor', 'tablet', 'pc'] },
    { id: 'accessories', name: 'Accessories', terms: ['accessory', 'charger', 'cable', 'case', 'adapter'] },
  ],
  apparel: [
    { id: 'women', name: 'Women', terms: ['women', 'woman', 'female', 'ladies'] },
    { id: 'men', name: 'Men', terms: ['men', 'man', 'male'] },
    { id: 'kids', name: 'Kids', terms: ['kids', 'kid', 'children', 'child', 'youth'] },
  ],
  fashion: [
    { id: 'women', name: 'Women', terms: ['women', 'woman', 'female', 'ladies'] },
    { id: 'men', name: 'Men', terms: ['men', 'man', 'male'] },
    { id: 'kids', name: 'Kids', terms: ['kids', 'kid', 'children', 'child', 'youth'] },
  ],
};

export const FILTER_GROUPS: FilterGroup[] = [
  {
    id: 'brand',
    label: 'Brand',
    options: [
      { value: '', label: 'All Brands' },
      { value: 'nike', label: 'Nike' },
      { value: 'adidas', label: 'Adidas' },
      { value: 'puma', label: 'Puma' },
    ],
  },
  {
    id: 'size',
    label: 'Size',
    options: [
      { value: '', label: 'All Sizes' },
      { value: '36', label: '36' },
      { value: '37', label: '37' },
      { value: '38', label: '38' },
      { value: '39', label: '39' },
    ],
  },
  {
    id: 'color',
    label: 'Colour',
    options: [
      { value: '', label: 'All Colours' },
      { value: 'black', label: 'Black' },
      { value: 'white', label: 'White' },
      { value: 'blue', label: 'Blue' },
      { value: 'red', label: 'Red' },
    ],
  },
  {
    id: 'qualities',
    label: 'Qualities',
    options: [
      { value: '', label: 'All Qualities' },
      { value: 'premium', label: 'Premium' },
      { value: 'sustainable', label: 'Sustainable' },
    ],
  },
  {
    id: 'price',
    label: 'Price',
    options: [
      { value: '', label: 'All Prices' },
      { value: '0-50', label: 'EUR0 - EUR50' },
      { value: '50-100', label: 'EUR50 - EUR100' },
      { value: '100+', label: 'EUR100+' },
    ],
  },
  {
    id: 'collection',
    label: 'Collection',
    options: [
      { value: '', label: 'All Collections' },
      { value: 'new', label: 'New in' },
      { value: 'sale', label: 'Sale' },
    ],
  },
  {
    id: 'material',
    label: 'Material',
    options: [
      { value: '', label: 'All Materials' },
      { value: 'leather', label: 'Leather' },
      { value: 'canvas', label: 'Canvas' },
      { value: 'synthetic', label: 'Synthetic' },
    ],
  },
  {
    id: 'heel',
    label: 'Type of heel',
    options: [
      { value: '', label: 'All Heel Types' },
      { value: 'flat', label: 'Flat' },
      { value: 'block', label: 'Block' },
    ],
  },
  {
    id: 'shoeWidth',
    label: 'Shoe width',
    options: [
      { value: '', label: 'All Widths' },
      { value: 'narrow', label: 'Narrow' },
      { value: 'regular', label: 'Regular' },
      { value: 'wide', label: 'Wide' },
    ],
  },
  {
    id: 'toe',
    label: 'Toe',
    options: [
      { value: '', label: 'All Toes' },
      { value: 'round', label: 'Round' },
      { value: 'pointed', label: 'Pointed' },
    ],
  },
];

export const createDefaultSelectedFilters = (): SelectedFilters => ({
  size: '',
  brand: '',
  price: '',
  color: '',
  qualities: '',
  collection: '',
  material: '',
  heel: '',
  shoeWidth: '',
  toe: '',
});

