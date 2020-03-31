import React, {PureComponent} from 'react';
import './index.css';
import productLogo from './product-logo-png.png';

class Logo extends PureComponent {
    render() {
        return (
            <div className="logo parent">
                <img alt="product-logo" src={productLogo}/>
            </div>
        )
    }
}

export default Logo;
