import React, { useState } from 'react';
import { createInputField, createTextArea } from '../utils/common';
import './styleguides.css';

const StyleGuide = () => {
    const [selectedButton, setSelectedButton] = useState(null);
    const [isPopupOpen, setIsPopupOpen] = useState(false);

    const handleClick = (buttonName) => {
        setSelectedButton(buttonName);
    };

    const togglePopup = () => {
        setIsPopupOpen(!isPopupOpen);
    };

    return (
        <div className="styleguide-container">
            <h1>Style Guide</h1>

            <section>
                <h2>Fonts</h2>
                <p className="bold-text">This is bold-text font</p>
                <p className="semibold-text">This is semibold-text font</p>
                <p className="medium-text">This is medium-text font</p>
                <p className="normal-text">This is normal-text font</p>
                <p className="highland-bold-text">This is highland-bold-text font</p>
                <p className="highland-normal-text">This is highland-normal-text font</p>
                <p className="highland-light-text">This is highland-light-text font</p>
            </section>

            <section>
                <h2>Buttons</h2>
                <button className="btn-primary">Primary Button</button>
                <button className="btn-secondary">Secondary Button</button>
                <button
                    className={`bold-text btn-header ${selectedButton === 'primary' ? 'selected' : ''}`}
                    onClick={() => handleClick('primary')}
                >
                    Primary Button
                </button>
            </section>

            <section>
                <h2>Popup</h2>
                <button className="btn-primary" onClick={togglePopup}>
                    Open Popup
                </button>

                {isPopupOpen && (
                    <div className="popup">
                        <div className="popup-title">Create File Source</div>
                        <div className="popup-content">
                            <form>
                                <div className="form-group">
                                    <label htmlFor="dataSourceName">Data Source Name *</label>
                                    {createInputField('text', 'dataSourceName', 'dataSourceName', true)}
                                </div>
                                <div className="form-group">
                                    <label htmlFor="description">Description</label>
                                    {createTextArea('description', 'description', 'description')}
                                </div>
                                <div className="popup-buttons">
                                    <button type="button" className="btn-close bold-text" onClick={togglePopup}>
                                        Close
                                    </button>
                                    <button type="button" className="btn-create bold-text">
                                        Next
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}
            </section>
        </div>
    );
};

export default StyleGuide;
