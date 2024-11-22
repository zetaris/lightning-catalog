import { createSlice } from '@reduxjs/toolkit';

const initialState = {
    queryResult: null,
    queryHistory: [],
};

const querySlice = createSlice({
    name: 'query',
    initialState,
    reducers: {
        setQueryResult: (state, action) => {
            state.queryResult = action.payload;
        },
        addQueryToHistory: (state, action) => {
            state.queryHistory.push(action.payload);
        },
    },
});

export const { setQueryResult, addQueryToHistory } = querySlice.actions;
export default querySlice.reducer;
