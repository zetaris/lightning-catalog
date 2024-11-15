import React, { useEffect } from 'react';
import AceEditor from 'react-ace';


import { defineCustomTheme, setupAceEditor, editorOptions } from '../../configs/editorConfig';

const Editor = ({ id, content, onChange, setEditorInstances }) => {
    useEffect(() => {
        defineCustomTheme();
    }, []);

    const handleEditorLoad = (editorInstance) => {
        if (editorInstance) {
            setupAceEditor(editorInstance);
            if (typeof setEditorInstances === 'function') {
                setEditorInstances((prevInstances) => ({
                    ...prevInstances,
                    [id]: editorInstance
                }));
            }
        }
    };    

    return (
        <AceEditor
            mode="sql"
            theme="myCustomTheme"
            name={`ace-editor-${id}`}
            fontSize={14}
            showPrintMargin={true}
            showGutter={true}
            highlightActiveLine={true}
            value={content}
            onChange={onChange}
            onLoad={handleEditorLoad}
            setOptions={{
                ...editorOptions,
                wrap: true,
            }}
            style={{ width: '100%', height: '100%' }}
        />
    );
};

export default Editor;
