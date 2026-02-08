import { FC } from 'react';

interface FilterOption {
  value: string;
  label: string;
}

interface FilterGroup {
  id: string;
  label: string;
  options: FilterOption[];
}

interface FilterBarProps {
  sortBy: 'name' | 'price' | 'rating';
  sortOrder: 'asc' | 'desc';
  activeFilter: string | null;
  filterGroups: FilterGroup[];
  selectedFilters: Record<string, string>;
  onToggleFilter: (id: string) => void;
  onSortChange: (value: string) => void;
  onFilterChange: (id: string, value: string) => void;
}

const FilterBar: FC<FilterBarProps> = ({
  sortBy,
  sortOrder,
  activeFilter,
  filterGroups,
  selectedFilters,
  onToggleFilter,
  onSortChange,
  onFilterChange,
}) => {
  return (
    <div className="filter-bar">
      <div className="filter-area">
        <div className="filter-buttons" aria-label="Filters">
          <div className="filter-dropdown">
            <button
              className={`filter-btn ${activeFilter === 'sort' ? 'active' : ''}`}
              onClick={() => onToggleFilter('sort')}
            >
              Sort by
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="6,9 12,15 18,9"></polyline>
              </svg>
            </button>
            {activeFilter === 'sort' && (
              <div className="filter-dropdown-menu">
                <button onClick={() => onSortChange('name-asc')} className={sortBy === 'name' && sortOrder === 'asc' ? 'selected' : ''}>
                  Most Popular
                  {sortBy === 'name' && sortOrder === 'asc' && <span className="checkmark">✓</span>}
                </button>
                <button onClick={() => onSortChange('name-desc')} className={sortBy === 'name' && sortOrder === 'desc' ? 'selected' : ''}>
                  Newest
                </button>
                <button onClick={() => onSortChange('price-asc')} className={sortBy === 'price' && sortOrder === 'asc' ? 'selected' : ''}>
                  Lowest Price
                </button>
                <button onClick={() => onSortChange('price-desc')} className={sortBy === 'price' && sortOrder === 'desc' ? 'selected' : ''}>
                  Highest Price
                </button>
                <button onClick={() => onSortChange('rating-desc')} className={sortBy === 'rating' && sortOrder === 'desc' ? 'selected' : ''}>
                  Deals
                </button>
              </div>
            )}
          </div>

          {filterGroups.map((filter) => (
            <div className="filter-dropdown" key={filter.id}>
              <button
                className={`filter-btn ${activeFilter === filter.id ? 'active' : ''}`}
                onClick={() => onToggleFilter(filter.id)}
              >
                {filter.label}
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="6,9 12,15 18,9"></polyline>
                </svg>
              </button>
              {activeFilter === filter.id && (
                <div className="filter-dropdown-menu">
                  {filter.options.map((option) => (
                    <button
                      key={option.value}
                      onClick={() => onFilterChange(filter.id, option.value)}
                      className={selectedFilters[filter.id] === option.value ? 'selected' : ''}
                    >
                      {option.label}
                    </button>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>

        <div className="filter-actions">
          <button className="filter-show-all" type="button">
            <span className="filter-icon" aria-hidden="true">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="4" y1="21" x2="4" y2="14"></line>
                <line x1="4" y1="10" x2="4" y2="3"></line>
                <line x1="12" y1="21" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12" y2="3"></line>
                <line x1="20" y1="21" x2="20" y2="16"></line>
                <line x1="20" y1="12" x2="20" y2="3"></line>
                <line x1="1" y1="14" x2="7" y2="14"></line>
                <line x1="9" y1="8" x2="15" y2="8"></line>
                <line x1="17" y1="16" x2="23" y2="16"></line>
              </svg>
            </span>
            Show all filters
          </button>
        </div>
      </div>
    </div>
  );
};
