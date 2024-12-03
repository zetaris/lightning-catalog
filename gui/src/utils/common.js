// Generalized API function using the /q endpoint for query execution
const executeQueryApi = async (query) => {
  try {
    const response = await fetch('http://localhost:8080/api/q', {
      method: 'POST',
      headers: {
        'Content-Type': 'text/plain',
      },
      body: query,
      mode: 'cors',
    });

    if (response.ok) {
      const result = await response.json();
      return result;
    } else {
      const errorText = await response.text();
      const cleanedText = errorText.replace(/[\u0000-\u001F\u007F-\u009F]/g, '').trim();
      const errorTextJson = JSON.parse(cleanedText)
      return {
        error: errorTextJson.error,
        message: errorTextJson.message || 'An unknown error occurred',
      };
    }
  } catch (error) {
    // console.error(`Request failed: ${error.message}`);
    return {
      error: true,
      message: error.message || 'Request failed',
    };
  }
};

export const qdqApi = async (name, table, validRecord, limit) => {
  try {
    const url = new URL('http://localhost:8080/api/qdq');
    url.searchParams.append('name', name);
    url.searchParams.append('table', table);
    url.searchParams.append('validRecord', validRecord);
    if (limit !== undefined) {
      url.searchParams.append('limit', limit);
    }

    const response = await fetch(url.toString(), {
      method: 'GET',
      mode: 'cors',
    });

    if (response.ok) {
      const rawText = await response.text();

      const fixedJson = rawText
        .trim()
        .replace(/,\]$/, ']');

      const result = JSON.parse(fixedJson).map((item) => JSON.parse(item));

      return result;
    } else {
      const error = await response.text();
      console.error('API Error:', error);
      return {
        error: true,
        message: `API error: ${response.status} ${response.statusText}`,
      };
    }
  } catch (error) {
    console.error('Request failed:', error);
    return {
      error: true,
      message: error.message || 'Request failed',
    };
  }
};

export const edqApi = async (name, table, validRecord) => {
  try {
    const url = new URL('http://localhost:8080/api/edq');
    url.searchParams.append('name', name);
    url.searchParams.append('table', table);
    url.searchParams.append('validRecord', validRecord);

    const response = await fetch(url.toString(), {
      method: 'GET',
      mode: 'cors',
    });

    if (response.ok) {
      const rawText = await response.text();

      const fixedJson = `[${rawText.trim()}]`;

      const parsedResult = JSON.parse(fixedJson).map((item) => {
        if (typeof item === "string") {
          return JSON.parse(item);
        }
        return item;
      });

      return parsedResult;
    } else {
      const errorText = await response.text();
      console.error('API Error:', errorText);
      return {
        error: true,
        message: `API error: ${response.status} ${response.statusText}`,
      };
    }
  } catch (error) {
    console.error('Request failed:', error);
    return {
      error: true,
      message: error.message || 'Request failed',
    };
  }
};

// Modified functions to use executeQueryApi

// Fetch API with a query parameter
export const fetchApi = async (query) => executeQueryApi(query);

// Save Semantic Layer using /q endpoint by passing in the DDL command
export const fetchApiForSave = async (ddlText, fileName = "") => {
  const query = `SAVE SEMANTIC LAYER AS '${fileName}' WITH QUERY ${ddlText}`;
  return executeQueryApi(query);
};

// Save file content with filename and extension using /q endpoint
export const fetchApiForSaveFiles = async (content, fileName = "default_file", extension = "json") => {
  const query = `SAVE FILE ${fileName}.${extension} WITH CONTENT ${JSON.stringify(content)}`;
  return executeQueryApi(query);
};

// Activate Table using /q endpoint
export const fetchActivateTableApi = async (requestData) => {
  const query = `ACTIVATE USL TABLE ${requestData.table} AS ${requestData.query}`;
  return executeQueryApi(query);
};

// Delete USL Table using /q endpoint
export const deleteActivateTableApi = async (fileName) => {
  const query = `DELETE USL TABLE '${fileName}'`;
  return executeQueryApi(query);
};

// Retrieve USL File Content using /q endpoint
export const fetchUSLFileContent = async (fileName) => {
  const query = `READ USL FILE '${fileName}'`;
  return executeQueryApi(query);
};


// export const fetchApi = async (query) => {
//   try {
//     const response = await fetch('http://localhost:8080/api/query', {
//       method: 'POST',
//       headers: {
//         'Content-Type': 'text/plain',
//       },
//       body: query,
//       mode: 'cors',
//     });

//     if (response.ok) {
//       const result = await response.json();

//       return result;
//     } else {
//       // If response is not OK, try to parse the error as JSON
//       try {
//         const error = await response.json();
//         console.error(`API Error: ${error.message}`);
//         return {
//           error: true,
//           message: error.message || 'An unknown error occurred',
//         };
//       } catch (parsingError) {
//         // If parsing fails, return the raw text response
//         const errorText = await response.text();
//         console.error(`API Error: ${errorText}`);
//         return {
//           error: true,
//           message: errorText || 'An unknown error occurred',
//         };
//       }
//     }
//   } catch (error) {
//     // Handle network or other fetch-related errors
//     console.error(`Request failed: ${error.message}`);
//     return {
//       error: true,
//       message: error.message || 'Request failed',
//     };
//   }
// };

// // Modified fetchApi function for saving semantic layer DDL
// export const fetchApiForSave = async (ddlText, fileName = "") => {
//   try {
//     const url = `http://localhost:8080/api/save-semantic-layer?fileName=${encodeURIComponent(fileName)}`;

//     const response = await fetch(url, {
//       method: 'POST',
//       headers: {
//         'Content-Type': 'application/json',
//       },
//       body: JSON.stringify(ddlText), // Pass the DDL text as part of JSON
//       mode: 'cors',
//     });

//     if (response.ok) {
//       const result = await response.json();
//       return result;
//     } else {
//       // If response is not OK, try to parse the error as JSON
//       try {
//         const error = await response.json();
//         console.error(`API Error: ${error.message}`);
//         return {
//           error: true,
//           message: error.message || 'An unknown error occurred',
//         };
//       } catch (parsingError) {
//         // If parsing fails, return the raw text response
//         const errorText = await response.text();
//         console.error(`API Error: ${errorText}`);
//         return {
//           error: true,
//           message: errorText || 'An unknown error occurred',
//         };
//       }
//     }
//   } catch (error) {
//     // Handle network or other fetch-related errors
//     console.error(`Request failed: ${error.message}`);
//     return {
//       error: true,
//       message: error.message || 'Request failed',
//     };
//   }
// };

// // JavaScript function to save content to a specified file with extension
// export const fetchApiForSaveFiles = async (content, fileName = "default_file", extension = "json") => {
//   try {
//     const url = `http://localhost:8080/api/save-to-file?fileName=${encodeURIComponent(fileName)}&extension=${encodeURIComponent(extension)}`;

//     const response = await fetch(url, {
//       method: 'POST',
//       headers: {
//         'Content-Type': 'application/json',
//       },
//       body: JSON.stringify(content), // Pass the content as JSON
//       mode: 'cors',
//     });

//     if (response.ok) {
//       const result = await response.json();
//       return result;
//     } else {
//       // If response is not OK, try to parse the error as JSON
//       try {
//         const error = await response.json();
//         console.error(`API Error: ${error.message}`);
//         return {
//           error: true,
//           message: error.message || 'An unknown error occurred',
//         };
//       } catch (parsingError) {
//         // If parsing fails, return the raw text response
//         const errorText = await response.text();
//         console.error(`API Error: ${errorText}`);
//         return {
//           error: true,
//           message: errorText || 'An unknown error occurred',
//         };
//       }
//     }
//   } catch (error) {
//     // Handle network or other fetch-related errors
//     console.error(`Request failed: ${error.message}`);
//     return {
//       error: true,
//       message: error.message || 'Request failed',
//     };
//   }
// };


// export const fetchApiForListSemanticLayers = async () => {
//   try {
//     const response = await fetch('http://localhost:8080/api/list-semantic-layers', {
//       method: 'GET',
//       headers: {
//         'Content-Type': 'application/json',
//       },
//       mode: 'cors',
//     });

//     if (response.ok) {
//       const result = await response.json();
//       return result; // Array of file names
//     } else {
//       // Handle non-OK response, try to parse error as JSON
//       try {
//         const error = await response.json();
//         console.error(`API Error: ${error.message}`);
//         return {
//           error: true,
//           message: error.message || 'An unknown error occurred',
//         };
//       } catch (parsingError) {
//         // If parsing fails, return the raw text response
//         const errorText = await response.text();
//         console.error(`API Error: ${errorText}`);
//         return {
//           error: true,
//           message: errorText || 'An unknown error occurred',
//         };
//       }
//     }
//   } catch (error) {
//     // Handle network or other fetch-related errors
//     console.error(`Request failed: ${error.message}`);
//     return {
//       error: true,
//       message: error.message || 'Request failed',
//     };
//   }
// };

// export const fetchCompileUSLApi = async (uslData) => {
//   try {
//     console.log(uslData);
//     const response = await fetch('http://localhost:8080/api/compile-usl', {
//       method: 'POST',
//       headers: {
//         'Content-Type': 'application/json',
//       },
//       body: JSON.stringify(uslData),
//       mode: 'cors',
//     });

//     if (response.ok) {
//       return await response.json();
//     } else {
//       const error = await response.json();
//       // console.error(`API Error: ${error.message}`);
//       return { error: true, message: error.message };
//     }
//   } catch (error) {
//     // console.error(`Request failed: ${error.message}`);
//     error.message = `Please check your DDL query.`;
//     return { error: true, message: error.message };
//   }
// };

// export const fetchActivateTableApi = async (requestData) => {
  
//   try {
//       const response = await fetch('http://localhost:8080/api/activate-usl-table', {
//           method: 'POST',
//           headers: {
//               'Content-Type': 'application/json'
//           },
//           body: JSON.stringify({
//             table: requestData.table,
//             query: requestData.query
//         })
//       });

//       const result = await response.json();
//       return result;
//   } catch (error) {
//       console.error("Error activating USL table:", error);
//   }
// };

// export const deleteActivateTableApi = async (fileName) => {
//   try {
//     const response = await fetch('http://localhost:8080/api/delete-usl-table', {
//       method: 'POST',
//       headers: {
//         'Content-Type': 'application/json'
//       },
//       body: JSON.stringify({ fileName })
//     });

//     if (response.ok) {
//       const data = await response.json();
//       // console.log("File deletion successful:", data.message);
//       return data.message;
//     } else {
//       const errorData = await response.json();
//       // console.error("Error deleting file:", errorData.message);
//     }
//   } catch (error) {
//     // console.error("Request failed:", error);
//   }
// }

// export const fetchUSLFileContent = async (fileName) => {
//   try {
//     const response = await fetch('http://localhost:8080/api/read-usl-file', {
//       method: 'POST',
//       headers: {
//         'Content-Type': 'application/json'
//       },
//       body: JSON.stringify({ fileName })
//     });

//     if (response.ok) {
//       const data = await response.json();
//       // console.log("File content retrieved:", data.content);
//       return data.content;
//     } else {
//       // const errorData = await response.json();
//       // console.error("Error retrieving file content:", errorData.message);
//       return null;
//     }
//   } catch (error) {
//     // console.error("Request failed:", error);
//     return null;
//   }
// }
