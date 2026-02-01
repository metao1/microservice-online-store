// Dependencies
import React, {Component} from 'react';
// Externals
import Cart from '../Cart';
import ShowProduct from '../ShowProduct';
import Products from '../Products';
import AdminPanel from "../Admin";
import Home from '../Home';
import {Footer, Navbar, Subscribe} from '../Main/components';
import {Route, Switch} from 'react-router-dom';
import './index.css';
import 'bootstrap/dist/css/bootstrap.css';

export default class App extends Component {
    userId = 'u1001';

    constructor(props) {
        super(props);
        this.state = {
            currency: '€',
            cart: {
                data: {},
                total: 0
            },
            scrolled: false,
            index: 0,
        };
    }

    componentWillUnmount() {
        window.removeEventListener('scroll');
    }

    componentDidMount() {
        this.fetchCart();
    }

    componentWillMount() {
        window.addEventListener('scroll', () => {
            let supportPageOffset = window.pageXOffset !== undefined;
            let isCSS1Compat = ((document.compatMode || '') === 'CSS1Compat');
            const scroll = {
                x: supportPageOffset ? window.pageXOffset : isCSS1Compat
                    ? document.documentElement.scrollLeft
                    : document.body.scrollLeft,
                y: supportPageOffset ? window.pageYOffset : isCSS1Compat
                    ? document.documentElement.scrollTop
                    : document.body.scrollTop
            };

            if (scroll.y > 50 && !this.state.scrolled) {
                this.setState({
                    scrolled: true
                });
            } else if (scroll.y < 50 && this.state.scrolled) {
                this.setState({
                    scrolled: false
                });
            }
        });
    }

    fetchCart = () => {
        const self = this;
        const url = 'http://localhost:8086/cart/' + this.userId;
        fetch(url, {
            method: "GET",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        })
        .then(res => res.json())
        .then(cart => this.setState({
            cart: {
                data: cart,
                total: self.totalReducer(cart),
                error: false
            }
        }));
    }

    totalReducer = (data) => {
        let sum = 0;
        for (const el in data) {
            if (data.hasOwnProperty(el)) {
                sum += parseFloat(data[el]);
            }
        }
        return sum;
    }

    addItemToCart = (product) => {
        if (product) {
            console.log("Added to Cart " + product.sku);
            const self = this;
            const url = 'http://localhost:8086/cart'
            var requestData = JSON.stringify(
                                          {user_id: this.userId,
                                           items:[{sku: product.sku, quantity: 1, price: product.price, currency: product.currency}]
                                           }
                                          )

            fetch(url, {
                method: 'POST',
                body: requestData,
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            })
            .then(res => res.json())
            .then(data => {
                self.setState({
                    cart: {
                        data: data,
                        total: self.totalReducer(data)
                    }
                });
            })
            .catch(error => {
                this.setState({
                    cart: {...this.state.cart, error: true}
                });

                setTimeout(() => this.setState({
                    cart: {...this.state.cart, error: false}
                }), 2500);
                console.warn('Request failed', error);

            });
        }
    };

    removeItemFromCart = (product) => {
        if (product) {
            console.log("Removed from Cart " + product.sku);
            const self = this;
            const url = 'http://localhost:8086/cart/' + this.userId + "/" + product.sku
            let requestData = new FormData();
            requestData.append("json", JSON.stringify({sku: product.sku}));

            fetch(url, {
                method: 'DELETE',
                body: requestData,
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            })
            .then(res => res.json())
            .then(data => {
                // const dataMerged = {};
                // const data = self.state.cart.data;
                // dataMerged[product.sku.sku || product.sku] = data[product.sku.sku || product.sku] ? data[product.sku.sku || product.sku] + 1 : 1;

                self.setState({
                    cart: {
                        data: data,
                        total: self.totalReducer(data)
                    }
                });
            })
            .catch(error => {
                this.setState({
                    cart: {...this.state.cart, error: true}
                });

                setTimeout(() => this.setState({
                    cart: {...this.state.cart, error: false}
                }), 2500);
                console.warn('Request failed', error);
            });
        }
    };

    render() {
        return (
            <div>
                <Navbar changeCurrency={(currencyUnit) => {
                    this.setState({currency: currencyUnit})
                }} cart={this.state.cart} scrolled={this.state.scrolled}/>
                <Switch>
                    <Route exact path="/"
                           render={(props) => (
                               <Home
                                   addItemToCart={this.addItemToCart}/>
                           )}/>

                    <Route path="/cart"
                           render={(props) => (
                               <Cart
                                   currency={this.state.currency}
                                   cart={this.state.cart}
                                   fetchCart={this.fetchCart}
                                   removeItemFromCart={this.removeItemFromCart}/>
                           )}/>
                    <Route path="/admin"
                           render={(props) => (
                               <AdminPanel currency={this.state.currency}/>
                           )}/>
                    <Route path="/music"
                           render={(props) => (
                               <Products
                                   currency={this.state.currency}
                                   category={"Music"}
                                   addItemToCart={this.addItemToCart}/>
                           )}/>
                    <Route path="/books"
                           render={(props) => (
                               <Products
                                   currency={this.state.currency}
                                   category={"Books"}
                                   addItemToCart={this.addItemToCart}/>
                           )}/>
                    <Route path="/beauty"
                           render={(props) => (
                               <Products
                                   currency={this.state.currency}
                                   category={"Beauty"}
                                   addItemToCart={this.addItemToCart}/>
                           )}/>
                    <Route path="/electronics"
                           render={(props) => (
                               <Products
                                   currency={this.state.currency}
                                   category={"Electronics"}
                                   addItemToCart={this.addItemToCart}/>
                           )}/>
                    <Route exact path="/:category"
                           render={(props) => (
                               <Products
                                   {...props}
                                   currency={this.state.currency}
                                   addItemToCart={this.addItemToCart}/>
                           )}/>

                    <Route path="/sort/:query"
                           render={(props) => (
                               <Products
                                   currency={this.state.currency}
                                   sort={props.match.params.query}
                                   addItemToCart={this.addItemToCart}/>
                           )}/>

                    <Route exact path="/products/:id" render={(props) => (
                        <ShowProduct currency={this.state.currency} {...props}
                                     addItemToCart={this.addItemToCart}/>
                    )}/>
                </Switch>
                <Subscribe/>
                <Footer/>
            </div>
        )
    }
}
