import { useState } from "react";

import { useNavigate } from "react-router-dom";

import api from "../Services/api";

import "./Login.css";

function Login() {

    const navigate = useNavigate();

    const [email, setEmail] = useState("");

    const [senha, setSenha] = useState("");

    async function fazerLogin(e) {

        e.preventDefault();

        try {

            const response = await api.post(
                    "/auth/login",
                    {
                        email,
                        senha
                    }
            );

            localStorage.setItem(
                    "token",
                    response.data.token
            );

            localStorage.setItem(
                    "usuarioId",
                    response.data.id
            );

            localStorage.setItem(
                    "role",
                    response.data.role
            );

            localStorage.setItem(
                    "email",
                    response.data.email
            );

            navigate("/home");

        } catch (error) {

            alert(
                    "Email ou senha inválidos"
            );
        }
    }

    return (

            <div className="login-container">

                <div className="login-box">

                    <h1>Sistema de Reservas</h1>

                    <form onSubmit={fazerLogin}>

                        <input
                                type="email"
                                placeholder="Email"
                                value={email}
                                onChange={(e) =>
                                        setEmail(
                                                e.target.value
                                        )
                                }
                        />

                        <input
                                type="password"
                                placeholder="Senha"
                                value={senha}
                                onChange={(e) =>
                                        setSenha(
                                                e.target.value
                                        )
                                }
                        />

                        <p
                                className="forgot"
                                onClick={() =>
                                        navigate(
                                                "/register"
                                        )
                                }
                        >
                            Não possui cadastro? Cadastre-se
                        </p>

                        <button type="submit">
                            Entrar
                        </button>

                    </form>

                </div>

            </div>
    );
}

export default Login;
