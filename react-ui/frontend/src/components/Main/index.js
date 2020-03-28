// Dependencies
import React, {Component} from 'react';
//Internals
import './index.css';
import {Footer, Navbar} from './components';

class Main extends Component {

    render() {
        return (
            <div>
                <Navbar/>
                {this.props.children}
                <Footer/>
            </div>
        );
    }
}

export default Main;
