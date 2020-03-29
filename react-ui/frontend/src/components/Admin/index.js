//Dependencies
import React, {Component} from 'react';
//Internals
import './index.css';
import {Button} from "../common";

class AdminPanel extends Component {
    asyncRequest = null;

    constructor(props) {
        super(props);
        this.state = {
            title: '',
            isCompleted: false
        };
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange(event) {
        this.setState({title: event.target.value});
    }

    handleSubmit(event) {
        alert('An essay was submitted: ' + this.state.value);
        event.preventDefault();
    }

    componentWillUnmount() {
        if (this._asyncRequest) {
            this._asyncRequest.cancel();
        }
    }

    submitProduct() {
        const url = '/api/products';
        let product = {
            asin: '1234567891',
            description: '1233',
            title: '12313',
            imageUrl: '13313',
            categories: ['Music'],
            price: 10
        };

        fetch(url, {
            method: 'POST',
            body: JSON.stringify(product),
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).catch(err => {
            console.log(err)
        }).then(result =>
            this.setState({result, isCompleted: true})
        );
    }

    render() {
        const self = this;
        return (
            <div className="cart-container">
                <div className="container">
                    <h5>Welcome to admin page where you can add new products to your repository</h5>
                    <span>Please fill in the form below and submit it</span>

                    <div className="publish-form">
                        <span>Title</span>
                        <input type="text" value={this.state.title} onChange={this.handleChange}/>
                        <div className="actions">
                            <Button onClick={() => {
                                this.submitProduct();
                                this.setState({isCompleted: true})
                            }} size="meduim" disabled={!Boolean(this.state.title)}>Publish</Button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default AdminPanel;
