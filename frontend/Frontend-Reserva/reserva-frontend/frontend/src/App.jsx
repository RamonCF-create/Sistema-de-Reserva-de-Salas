import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

import Login from "./Pages/Login";

import Register from "./Pages/Register";

import Home from "./Pages/Home";

import PrivateRoute from "./Routes/PrivateRoute";

function App() {

    return (

            <BrowserRouter>

                <Routes>

                    <Route
                            path="/"
                            element={<Login />}
                    />

                    <Route
                            path="/login"
                            element={<Login />}
                    />

                    <Route
                            path="/register"
                            element={<Register />}
                    />

                    <Route
                            path="/home"
                            element={
                                <PrivateRoute>
                                    <Home />
                                </PrivateRoute>
                            }
                    />

                </Routes>

            </BrowserRouter>
    );
}

export default App;
