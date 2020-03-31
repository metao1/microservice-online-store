// Dependencies
import React, {Component} from 'react';
import {Logo} from '../';
import {Icon} from "../../../common";
import {NavLink, withRouter} from 'react-router-dom';
// Internals
import './index.css';
import Dropdown from "react-bootstrap/Dropdown";

class Navbar extends Component {
    state = {itemsInCart: 0}

    constructor(props) {
        super(props);
        this.state = {
            currency: '€ Euro',
            itemsInCart: 0,
        };
    }

    componentDidMount() {
        // TODO: add this
        //fetch('http://localhost:3001/cart/itemsInCart')
        //  .then(res => res.json())
        //  .then(itemsInCart => this.setState({ itemsInCart }));
    }

    render() {
        const {match, location, history} = this.props
        const notIndex = location.pathname !== "/";
        return (
            <nav className={`nav-bar ${this.props.scrolled || notIndex ? 'nav-bar-scrolled' : ''}`}>
                <NavLink to="/">
                    <Logo mode={this.props.scrolled || notIndex ? 'dark' : 'light'}/>
                </NavLink>
                <div className="nav-links">
                    <ul>
                        <li><NavLink activeClassName="selected" className="nav-link" to="/Books">
                            <span className="nav-link-icon"><Icon icon="book"/></span>
                            <span className="nav-link-text">Books</span>
                        </NavLink></li>
                        <li><NavLink activeClassName="selected" className="nav-link" to="/Music">
                            <span className="nav-link-icon"><Icon icon="music"/></span>
                            <span className="nav-link-text">Music</span>
                        </NavLink></li>
                        <li><NavLink activeClassName="selected" className="nav-link" to="/Beauty">
                            <span className="nav-link-icon"><Icon icon="makeup"/></span>
                            <span className="nav-link-text">Beauty</span>
                        </NavLink></li>
                        <li><NavLink activeClassName="selected" className="nav-link" to="/Electronics">
                            <span className="nav-link-icon"><Icon icon="plug"/></span>
                            <span className="nav-link-text">Electronics</span>
                        </NavLink></li>
                    </ul>
                </div>
                <div className='nav-setting'>
                    <div className='nav-cart'>
                        <NavLink className={`${this.props.cart.total ? 'nav-cart-active' : ''}`} to="/cart">
                            <Dropdown>
                                <Dropdown.Toggle variant="success" id="dropdown-basic">
                                    {this.state.currency}
                                </Dropdown.Toggle>
                                <Dropdown.Menu>
                                    <Dropdown.Item onClick={() => this.setState({currency: '€ Euro'})}>€
                                        Euro</Dropdown.Item>
                                    <Dropdown.Item onClick={() => this.setState({currency: '$ Dollar'})}>$
                                        Dollar</Dropdown.Item>
                                    <Dropdown.Item onClick={() => this.setState({currency: '£ Pound'})}>£
                                        Pound</Dropdown.Item>
                                </Dropdown.Menu>
                            </Dropdown>
                        </NavLink>
                    </div>
                    <div className='nav-cart'>
                        <NavLink className={`${this.props.cart.total ? 'nav-cart-active' : ''}`} to="/cart">
                            {this.props.cart.total > 0 && <span
                                className={`nav-cart-count ${this.props.cart.error ? "nav-cart-count-error" : ""}`}>{this.props.cart.total}</span>}
                            <Icon icon="cart" color={this.props.scrolled || notIndex ? '#000000' : '#ffffff'}/>Cart
                        </NavLink>
                    </div>
                    <div className='nav-cart'>
                        <NavLink className='nav-cart-active' to="/admin">
                            <Icon icon="scifi" color={this.props.scrolled || notIndex ? '#000000' : '#ffffff'}/>Admin
                        </NavLink>
                    </div>
                </div>
            </nav>
        )
    }
}

export default withRouter(Navbar);
