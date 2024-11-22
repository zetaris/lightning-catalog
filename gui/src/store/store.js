import { configureStore } from '@reduxjs/toolkit';
import queryReducer from './querySlice';

const store = configureStore({
    reducer: {
        query: queryReducer,
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware({
            serializableCheck: false,
        }),
});

export default store;
