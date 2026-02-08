import { FC } from 'react';

interface ProductCountProps {
  count: number;
}

const ProductCount: FC<ProductCountProps> = ({ count }) => {
  return (
    <div className="product-count">
      <span>{count.toLocaleString()} items</span>
      <span className="info-icon">i</span>
    </div>
  );
};

export default ProductCount;
