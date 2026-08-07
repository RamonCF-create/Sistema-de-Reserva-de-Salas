import { useState } from "react";

import { useNavigate } from "react-router-dom";

import api from "../Services/api";

import "./Login.css";

function Register() {

    const navigate = useNavigate();

    const [email, setEmail] = useState("");

    const [senha, setSenha] = useState("");

    async function fazerCadastro(e) {

        e.preventDefault();

        try {

            await api.post(
                "/auth/register",
                {
                    email,
                    senha
                }
            );

            alert("Cadastro realizado com sucesso");

            navigate("/login");

        } catch (error) {

            alert(
                error.response?.data?.message
                || "Erro no cadastro"
            );
        }
    }

    return (

        <div className="login-container">

            <div className="login-box">

                <h1>Criar Conta</h1>

                <form onSubmit={fazerCadastro}>

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

                    <button type="submit">
                        Cadastrar
                    </button>

                </form>

            </div>

        </div>
    );
}

export default Register;
