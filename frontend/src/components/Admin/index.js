//Dependencies
import React, {Component} from 'react';
//Internals
import './index.css';
import {Button} from "../common";
import Select from 'react-select'
import CurrencyFormat from 'react-currency-format';

class AdminPanel extends Component {
    _asyncRequest = null;
    options = [];

    constructor(props) {
        super(props);
        this.state = {
            title: '',
            loading: false,
            description: '',
            imageUrl: '',
            price: 0,
            categories: undefined,
            isCompleted: false
        };
        this.handleChange = this.handleChange.bind(this);
        this.onCategoriesChanged = this.onCategoriesChanged.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange(event) {
        if (event && event.target) {
            let nam = event.target.name;
            let val = event.target.value;
            this.setState({[nam]: val});
        }
    }

    onCategoriesChanged(event) {
        if (!!event) {
            this.setState({categories: event.map(ev => ev.value)});
        }
    }

    handleSubmit(event) {
        alert('An essay was submitted: ' + this.state.value);
        event.preventDefault();
    }

    fetchCategories() {
        const url = '/api/products/categories';
        console.log("Fetching categories: " + url);
        this.setState({loading: true});
        this._asyncRequest = fetch(url)
        .then(res => res.json())
        .then(categories => {
            this._asyncRequest = null;
            categories.slice(0, 1200)
            .sort(this.sortIt)
            .filter(this.onlyUnique)
            .flatMap(item => {
                this.options.push(
                    {value: item.categories, label: item.categories});
            });
            this.setState({loading: false});
        }).catch(_ => {
            this.createNotification('error', 'can not get the categories');
            console.error('no categories found');
        });
    }

    sortIt(a, b) {
        return a.categories - b.categories;
    }

    onlyUnique(value, index, self) {
        return self.indexOf(value) === index;
    }

    submitProduct() {
        const url = '/products';
        let product = {
            description: this.state.description,
            title: this.state.title,
            imageUrl: this.state.imageUrl,
            categories: this.state.categories,
            price: this.state.price
        };
        this._asyncRequest = fetch(url, {
            method: 'POST',
            body: JSON.stringify(product),
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).catch(err => {
            this.createNotification('error', 'can not submit the product');
            console.log(err);
        }).then(result => {
                this.setState({result, isCompleted: true})
                this.createNotification('info', 'product saved');
            }
        ).finally(() => {
            this.resetForm(null);
        });
    }

    resetForm() {
        this.setState({
            title: '',
            description: '',
            imageUrl: '',
            price: 0,
            categories: undefined,
            loading: false,
            isCompleted: false
        });
    }

    render() {
        return (
            <div className="cart-container">
                <div className="container">
                    <h5>Welcome to admin page where you can add new products to
                        your repository</h5>
                    <span>Please fill in the form below and submit it</span>
                    <div className="publish-form">
                        <span>Title</span>
                        <input type="text" name="title" value={this.state.title}
                               onChange={this.handleChange} required/>
                        <span>Description</span>
                        <input type="text" name="description"
                               value={this.state.description}
                               onChange={this.handleChange}/>
                        <span>Image URL</span>
                        <input type="text" name="imageUrl"
                               value={this.state.imageUrl}
                               onChange={this.handleChange}
                               required/>
                        <span>Price</span>
                        <CurrencyFormat name="price" value={this.state.price}
                                        thousandSeparator={true}
                                        prefix={this.props.currency}
                                        onValueChange={(values) => {
                                            const {
                                                formattedValue,
                                                value
                                            } = values;
                                            this.setState(
                                                {price: formattedValue});
                                        }}/>
                        <div className="margin-top-10">
                            <div className="margin-top-10">
                                <span>Categories</span>
                                <Select onChange={this.onCategoriesChanged}
                                        onMenuOpen={this.loadCategories()}
                                        isLoading={Boolean(this.state.loading)}
                                        closeMenuOnSelect={false} isMulti
                                        options={this.options}/>
                            </div>
                            <br/>
                            <div className="actions">
                                <Button onClick={() => {
                                    this.submitProduct();
                                    this.setState({isCompleted: true})
                                }} size="medium"
                                        disabled={!Boolean(
                                            this.state.title)}>Publish</Button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    createNotification = (type, content) => {
        //alert(content);
    };

    loadCategories() {
        if (this.options.length === 0 && !Boolean(this.state.loading)) {
            this.fetchCategories();
        }
    }
}

export default AdminPanel;
