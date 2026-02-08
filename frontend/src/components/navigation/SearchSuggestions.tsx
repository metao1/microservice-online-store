import { FC } from 'react';
import { SearchIcon } from './icons';

interface SearchSuggestionsProps {
  isOpen: boolean;
  isLoading: boolean;
  suggestions: string[];
  query: string;
  onSelect: (value: string) => void;
}

const SearchSuggestions: FC<SearchSuggestionsProps> = ({
  isOpen,
  isLoading,
  suggestions,
  query,
  onSelect,
}) => {
  if (!isOpen) return null;

  return (
    <div className="search-suggestions" data-testid="search-suggestions">
      {isLoading ? (
        <div className="search-suggestion loading-suggestion">
          <SearchIcon className="suggestion-icon" />
          Searching...
        </div>
      ) : suggestions.length > 0 ? (
        suggestions.map((suggestion, index) => (
          <button
            key={`${suggestion}-${index}`}
            type="button"
            className="search-suggestion"
            onClick={() => onSelect(suggestion)}
            data-testid={`search-suggestion-${index}`}
          >
            <SearchIcon className="suggestion-icon" />
            {suggestion}
          </button>
        ))
      ) : query.length > 2 ? (
        <div className="search-suggestion no-results">
          <SearchIcon className="suggestion-icon" />
          No suggestions found
        </div>
      ) : null}
    </div>
  );
};

export default SearchSuggestions;
