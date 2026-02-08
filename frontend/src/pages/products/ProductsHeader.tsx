import { FC } from 'react';

interface TabItem {
  id: string;
  name: string;
}

interface ProductsHeaderProps {
  rootLabel: string;
  currentLabel: string;
  title: string;
  tabs: TabItem[];
  activeTab: string;
  onTabClick: (id: string) => void;
}

const ProductsHeader: FC<ProductsHeaderProps> = ({
  rootLabel,
  currentLabel,
  title,
  tabs,
  activeTab,
  onTabClick,
}) => {
  return (
    <div className="products-header">
      <div className="breadcrumb">
        <span>{rootLabel}</span>
        <span className="breadcrumb-separator">›</span>
        <span>{currentLabel}</span>
      </div>

      <h1 className="page-title">{title}</h1>

      <div className="segment-tabs">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            className={`segment-tab ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => onTabClick(tab.id)}
          >
            {tab.name}
          </button>
        ))}
      </div>
    </div>
  );
};

export default ProductsHeader;
