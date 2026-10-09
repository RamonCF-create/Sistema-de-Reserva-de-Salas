import { useEffect, useMemo, useState } from 'react';
import api from '../Services/api';

const reservaInicial = {
  ambienteId: '',
  professor: '',
  data: '',
  horarioInicio: '',
  horarioFim: ''
};

const ambienteInicial = {
  nome: '',
  capacidade: '',
  fichaTecnica: ''
};

function Home() {
  const role = localStorage.getItem('role') || 'USER';
  const email = localStorage.getItem('email') || '';
  const isAdmin = role === 'ADMIN';
  const usuarioId = Number(localStorage.getItem('usuarioId'));


  const [paginaAtual, setPaginaAtual] = useState('dashboard');
  const [reservas, setReservas] = useState([]);
  const [ambientes, setAmbientes] = useState([]);
  const [notificacoes, setNotificacoes] = useState([]);
  const [usuarios, setUsuarios] = useState([]);
  const [reservaForm, setReservaForm] = useState(reservaInicial);
  const [ambienteForm, setAmbienteForm] = useState(ambienteInicial);
  const [disponibilidadeFiltro, setDisponibilidadeFiltro] = useState({
    data: '',
    horarioInicio: '',
    horarioFim: ''
  });
  const [disponibilidadeAmbientes, setDisponibilidadeAmbientes] = useState([]);
  const [disponibilidadeCarregando, setDisponibilidadeCarregando] = useState(false);
  const [reservaFiltroData, setReservaFiltroData] = useState('');
  const [reservaDataFiltrada, setReservaDataFiltrada] = useState('');
  const [reservaEditandoId, setReservaEditandoId] = useState(null);
  const [ambienteEditandoId, setAmbienteEditandoId] = useState(null);
  const [mapaData, setMapaData] = useState('');
  const [mapaCarregando, setMapaCarregando] = useState(false);
  const [erro, setErro] = useState('');
  const [mensagem, setMensagem] = useState('');

  const notificacoesNaoLidas = useMemo(
    () => notificacoes.filter((notificacao) => !notificacao.lida).length,
    [notificacoes]
  );

  const disponibilidadePorAmbiente = useMemo(() => {
    return disponibilidadeAmbientes.reduce((mapa, ambiente) => {
      mapa[ambiente.id] = ambiente;
      return mapa;
    }, {});
  }, [disponibilidadeAmbientes]);

  const reservasFiltradas = useMemo(() => {
    if (!reservaDataFiltrada) {
      return reservas;
    }

    return reservas.filter((reserva) => reserva.data === reservaDataFiltrada);
  }, [reservas, reservaDataFiltrada]);

  useEffect(() => {
    carregarDados();
  }, []);

  async function carregarDados() {
    const requisicoes = [
      listarReservas(),
      listarAmbientes(),
      listarNotificacoes()
    ];

    if (isAdmin) {
      requisicoes.push(listarUsuarios());
    }

    await Promise.all(requisicoes);
  }

  async function listarReservas() {
    try {
      const response = await api.get('/reservas');
      setReservas(response.data);
    } catch (error) {
      console.error('Erro ao listar reservas:', error);
      setErro('Erro ao carregar reservas');
    }
  }

  async function listarAmbientes() {
    try {
      const response = await api.get('/ambientes');
      setAmbientes(response.data);
    } catch (error) {
      console.error('Erro ao listar ambientes:', error);
      setErro('Erro ao carregar ambientes');
    }
  }

  async function listarNotificacoes() {
    try {
      const response = await api.get('/notificacoes');
      setNotificacoes(response.data);
    } catch (error) {
      console.error('Erro ao listar notificacoes:', error);
    }
  }

  async function listarUsuarios() {
    try {
      const response = await api.get('/usuarios');
      setUsuarios(response.data);
    } catch (error) {
      console.error('Erro ao listar usuarios:', error);
      setErro('Erro ao carregar usuarios');
    }
  }

  function limparMensagens() {
    setErro('');
    setMensagem('');
  }

  function alterarReserva(campo, valor) {
    setReservaForm((form) => ({
      ...form,
      [campo]: valor
    }));
  }

  function alterarAmbiente(campo, valor) {
    setAmbienteForm((form) => ({
      ...form,
      [campo]: valor
    }));
  }

  function alterarDisponibilidade(campo, valor) {
    setDisponibilidadeFiltro((filtro) => ({
      ...filtro,
      [campo]: valor
    }));
  }

  async function salvarReserva(event) {
    event.preventDefault();
    limparMensagens();

    if (
      !reservaForm.ambienteId ||
      !reservaForm.professor ||
      !reservaForm.data ||
      !reservaForm.horarioInicio ||
      !reservaForm.horarioFim
    ) {
      setErro('Preencha todos os campos da reserva');
      return;
    }

    if (reservaForm.horarioFim <= reservaForm.horarioInicio) {
      setErro('O horario final deve ser depois do horario inicial');
      return;
    }

    
    const [horaInicio, minutoInicio] =
      reservaForm.horarioInicio.split(':').map(Number);

    const [horaFim, minutoFim] =
      reservaForm.horarioFim.split(':').map(Number);

    const inicioEmMinutos = horaInicio * 60 + minutoInicio;
    const fimEmMinutos = horaFim * 60 + minutoFim;

    const duracaoMinutos = fimEmMinutos - inicioEmMinutos;

    if (duracaoMinutos < 15) {
      setErro('A reserva deve ter duração mínima de 15 minutos.');
      return;
    }


    try {
      if (reservaEditandoId) {
        await api.put(`/reservas/${reservaEditandoId}`, reservaForm);
        setMensagem('Reserva atualizada');
      } else {
        await api.post('/reservas', reservaForm);
        setMensagem('Reserva solicitada');
      }

      setReservaForm(reservaInicial);
      setReservaEditandoId(null);
      setPaginaAtual('reservas');
      await carregarDados();
        } catch (error) {
      console.error('Erro ao salvar reserva:', error);
      const mensagemErro = error.response?.data?.message || error.response?.data || 'Erro ao salvar reserva';
      setErro(mensagemErro);
    }
  }

  function editarReserva(reserva) {
    setReservaForm({
      ambienteId: reserva.ambiente?.id || '',
      professor: reserva.professor || '',
      data: reserva.data || '',
      horarioInicio: reserva.horarioInicio || '',
      horarioFim: reserva.horarioFim || ''
    });
    setReservaEditandoId(reserva.id);
    setPaginaAtual('reserva-form');
  }

  async function atualizarStatus(id, status) {
    limparMensagens();

    try {
      await api.patch(`/reservas/${id}/status`, { status });
      setMensagem(`Reserva ${status.toLowerCase()}`);
      await carregarDados();
    } catch (error) {
      console.error('Erro ao atualizar status:', error);
      setErro('Erro ao atualizar status');
    }
  }

  async function deletarReserva(id) {
    if (!window.confirm('Tem certeza que deseja excluir esta reserva?')) {
      return;
    }

    limparMensagens();

    try {
      await api.delete(`/reservas/${id}`);
      setMensagem('Reserva excluida');
      await carregarDados();
    } catch (error) {
      console.error('Erro ao excluir reserva:', error);
      setErro('Erro ao excluir reserva');
    }
  }

  function filtrarReservasPorData(event) {
    event.preventDefault();
    limparMensagens();

    if (!reservaFiltroData) {
      setErro('Selecione uma data para filtrar as reservas');
      return;
    }

    setReservaDataFiltrada(reservaFiltroData);
  }

  function limparFiltroReservas() {
    limparMensagens();
    setReservaFiltroData('');
    setReservaDataFiltrada('');
  }

  async function salvarAmbiente(event) {
    event.preventDefault();
    limparMensagens();

    if (!ambienteForm.nome || !ambienteForm.capacidade) {
      setErro('Informe nome e capacidade do ambiente');
      return;
    }

    const payload = {
      ...ambienteForm,
      capacidade: Number(ambienteForm.capacidade)
    };

    try {
      if (ambienteEditandoId) {
        await api.put(`/ambientes/${ambienteEditandoId}`, payload);
        setMensagem('Ambiente atualizado');
      } else {
        await api.post('/ambientes', payload);
        setMensagem('Ambiente criado');
      }

      setAmbienteForm(ambienteInicial);
      setAmbienteEditandoId(null);
      await listarAmbientes();
      setPaginaAtual('ambientes');
    
    } catch (error) {
      console.error('Erro ao salvar ambiente:', error);

      setErro(
        error.response?.data?.message ||
        'Erro ao salvar ambiente'
      );
    }

  }

  function editarAmbiente(ambiente) {
    setAmbienteForm({
      nome: ambiente.nome || '',
      capacidade: ambiente.capacidade || '',
      fichaTecnica: ambiente.fichaTecnica || ''
    });
    setAmbienteEditandoId(ambiente.id);
    setPaginaAtual('ambiente-form');
  }

  async function deletarAmbiente(id) {
    if (!window.confirm('Tem certeza que deseja excluir este ambiente?')) {
      return;
    }

    limparMensagens();

    try {
      await api.delete(`/ambientes/${id}`);
      setMensagem('Ambiente excluido');
      await listarAmbientes();
    } catch (error) {
      console.error('Erro ao excluir ambiente:', error);
      setErro('Erro ao excluir ambiente');
    }
  }

  async function consultarDisponibilidade(event) {
    event.preventDefault();
    limparMensagens();

    if (
      !disponibilidadeFiltro.data ||
      !disponibilidadeFiltro.horarioInicio ||
      !disponibilidadeFiltro.horarioFim
    ) {
      setErro('Informe data, horario inicial e horario final para consultar a disponibilidade');
      return;
    }

    if (disponibilidadeFiltro.horarioFim <= disponibilidadeFiltro.horarioInicio) {
      setErro('O horario final deve ser depois do horario inicial');
      return;
    }

    
    const [horaInicio, minutoInicio] =
      disponibilidadeFiltro.horarioInicio.split(':').map(Number);

    const [horaFim, minutoFim] =
      disponibilidadeFiltro.horarioFim.split(':').map(Number);

    const inicioEmMinutos = horaInicio * 60 + minutoInicio;
    const fimEmMinutos = horaFim * 60 + minutoFim;

    const duracaoMinutos = fimEmMinutos - inicioEmMinutos;

    if (duracaoMinutos < 15) {
      setErro(
        'O período consultado deve ter duração mínima de 15 minutos.'
      );
      return;
    }


    setDisponibilidadeCarregando(true);

    try {
      const response = await api.get('/ambientes/disponibilidade', {
        params: disponibilidadeFiltro
      });

      setDisponibilidadeAmbientes(response.data);
      setMensagem('Disponibilidade atualizada');
    } catch (error) {
      console.error('Erro ao consultar disponibilidade:', error);
      setErro(error.response?.data?.message || 'Erro ao consultar disponibilidade');
    } finally {
      setDisponibilidadeCarregando(false);
    }
  }

  async function marcarComoLida(id) {
    try {
      await api.patch(`/notificacoes/${id}/lida`);
      await listarNotificacoes();
    } catch (error) {
      console.error('Erro ao marcar notificacao:', error);
    }
  }

  async function alterarPerfilUsuario(id, novoPerfil) {
    limparMensagens();

    try {
      await api.patch(`/usuarios/${id}/role`, { role: novoPerfil });
      setMensagem('Perfil do usuario atualizado');
      await listarUsuarios();
    } catch (error) {
      console.error('Erro ao alterar perfil:', error);
      setErro(error.response?.data?.message || 'Erro ao alterar perfil do usuario');
    }
  }

  async function gerarMapaDiario(event) {
    event.preventDefault();
    limparMensagens();

    if (!mapaData) {
      setErro('Selecione uma data para gerar o mapa diario');
      return;
    }

    setMapaCarregando(true);

    try {
      const response = await api.get('/reservas/pdf', {
        params: { data: mapaData },
        responseType: 'blob'
      });

      const pdfUrl = URL.createObjectURL(
        new Blob([response.data], { type: 'application/pdf' })
      );

      window.open(pdfUrl, '_blank', 'noopener,noreferrer');
      setMensagem('Mapa diario gerado com sucesso');
    } catch (error) {
      console.error('Erro ao gerar mapa diario:', error);
      const mensagemErro = await obterMensagemErroPdf(error);
      setErro(mensagemErro || 'Erro ao gerar mapa diario');
    } finally {
      setMapaCarregando(false);
    }
  }

  async function deletarUsuario(id) {
    if (!window.confirm('Tem certeza que deseja excluir este usuario?')) {
      return;
    }

    limparMensagens();

    try {
      await api.delete(`/usuarios/${id}`);
      setMensagem('Usuario excluido');
      await listarUsuarios();
    } catch (error) {
      console.error('Erro ao excluir usuario:', error);
      setErro(error.response?.data?.message || 'Erro ao excluir usuario');
    }
  }

  function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('usuarioId');
    localStorage.removeItem('role');
    localStorage.removeItem('email');
    window.location.href = '/login';
  }

  const menuItems = [
  { id: 'dashboard', nome: 'Dashboard' },
  { id: 'reservas', nome: 'Reservas' },
  { id: 'reserva-form', nome: reservaEditandoId ? 'Editar reserva' : 'Solicitar reserva' },
  { id: 'ambientes', nome: 'Ambientes' },

  ...(isAdmin
    ? [{
        id: 'ambiente-form',
        nome: ambienteEditandoId
          ? 'Editar ambiente'
          : 'Novo ambiente'
      }]
    : []),

  {
    id: 'notificacoes',
    nome: `Notificacoes${notificacoesNaoLidas ? ` (${notificacoesNaoLidas})` : ''}`
  },

  ...(isAdmin ? [{ id: 'usuarios', nome: 'Usuarios' }] : []),
  ...(isAdmin ? [{ id: 'aprovacoes', nome: 'Aprovacoes' }] : []),
  ...(isAdmin ? [{ id: 'mapa-diario', nome: 'Mapa diario' }] : [])
];

  const pendentes = reservas.filter((reserva) => reserva.status === 'PENDENTE');

  return (
    <div style={styles.container}>
      <aside style={styles.sidebar}>
        <div style={styles.logo}>
          <strong>Sistema de Reservas</strong>
          <span style={styles.userInfo}>{email}</span>
          <span style={styles.role}>{role}</span>
          <button onClick={logout} style={styles.logout}>Sair</button>
        </div>

        <nav>
          {menuItems.map((item) => (
            <button
              key={item.id}
              onClick={() => setPaginaAtual(item.id)}
              style={{
                ...styles.menuItem,
                ...(paginaAtual === item.id ? styles.menuItemActive : {})
              }}
            >
              {item.nome}
            </button>
          ))}
        </nav>
      </aside>

      <main style={styles.content}>
        {erro && <div style={styles.erro}>{erro}</div>}
        {mensagem && <div style={styles.sucesso}>{mensagem}</div>}

        {paginaAtual === 'dashboard' && (
          <section style={styles.panel}>
            <h1 style={styles.title}>Dashboard</h1>
            <div style={styles.metrics}>
              <Metric label="Reservas" value={reservas.length} />
              <Metric label="Pendentes" value={pendentes.length} />
              <Metric label="Aprovadas" value={reservas.filter((r) => r.status === 'APROVADA').length} />
              <Metric label="Rejeitadas" value={reservas.filter((r) => r.status === 'REJEITADA').length} />
              <Metric label="Ambientes" value={ambientes.length} />
              <Metric label="Nao lidas" value={notificacoesNaoLidas} />
            </div>
          </section>
        )}

        {paginaAtual === 'reserva-form' && (
          <section style={styles.panel}>
            <h1 style={styles.title}>{reservaEditandoId ? 'Editar reserva' : 'Solicitar reserva'}</h1>
            <form onSubmit={salvarReserva} style={styles.form}>
              <label style={styles.formGroup}>
                Ambiente
                <select
                  value={reservaForm.ambienteId}
                  onChange={(event) => alterarReserva('ambienteId', event.target.value)}
                  style={styles.input}
                >
                  <option value="">Selecione um ambiente</option>
                  {ambientes.map((ambiente) => (
                    <option key={ambiente.id} value={ambiente.id}>
                      {ambiente.nome}
                    </option>
                  ))}
                </select>
              </label>

              <label style={styles.formGroup}>
                Professor
                <input
                  value={reservaForm.professor}
                  onChange={(event) => alterarReserva('professor', event.target.value)}
                  style={styles.input}
                />
              </label>

              <label style={styles.formGroup}>
                Data
               <input
                  type="date"
                  value={reservaForm.data}
                  onChange={(event) => alterarReserva('data', event.target.value)}
                  min={new Date().toISOString().split('T')[0]}
                  style={styles.input}
                />
              </label>

              <label style={styles.formGroup}>
                Horario inicial
                <input
                  type="time"
                  value={reservaForm.horarioInicio}
                  onChange={(event) => alterarReserva('horarioInicio', event.target.value)}
                  style={styles.input}
                />
              </label>

              <label style={styles.formGroup}>
                Horario final
                <input
                  type="time"
                  value={reservaForm.horarioFim}
                  onChange={(event) => alterarReserva('horarioFim', event.target.value)}
                  style={styles.input}
                />
              </label>

              <div style={styles.actions}>
                <button type="submit" style={styles.primaryButton}>
                  {reservaEditandoId ? 'Atualizar' : 'Solicitar'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setReservaForm(reservaInicial);
                    setReservaEditandoId(null);
                  }}
                  style={styles.secondaryButton}
                >
                  Limpar
                </button>
              </div>
            </form>
          </section>
        )}

        {paginaAtual === 'reservas' && (
          <section style={styles.panel}>
            <h1 style={styles.title}>Reservas</h1>
            <form onSubmit={filtrarReservasPorData} style={styles.filterForm}>
              <label style={styles.formGroup}>
                Data
                <input
                  type="date"
                  value={reservaFiltroData}
                  onChange={(event) => setReservaFiltroData(event.target.value)}
                  style={styles.input}
                />
              </label>

              <div style={styles.actions}>
                <button type="submit" style={styles.primaryButton}>
                  Filtrar
                </button>
                <button type="button" onClick={limparFiltroReservas} style={styles.secondaryButton}>
                  Limpar
                </button>
              </div>
            </form>

            {reservaDataFiltrada && (
              <p style={styles.muted}>
                Exibindo reservas de {reservaDataFiltrada}, ordenadas por horario inicial.
              </p>
            )}

            <TabelaReservas
              reservas={reservasFiltradas}
              isAdmin={isAdmin}
              usuarioId={usuarioId}
              mostrarEdicao
              mostrarExclusao={isAdmin}
              onEditar={editarReserva}
              onDeletar={deletarReserva}
            />
          </section>
        )}

        {paginaAtual === 'aprovacoes' && isAdmin && (
          <section style={styles.panel}>
            <h1 style={styles.title}>Aprovacoes pendentes</h1>
            <TabelaReservas
              reservas={pendentes}
              isAdmin={isAdmin}
              mostrarAprovacao
              onStatus={atualizarStatus}
            />
          </section>
        )}

        {paginaAtual === 'ambiente-form' && (
          <section style={styles.panel}>
            <h1 style={styles.title}>{ambienteEditandoId ? 'Editar ambiente' : 'Novo ambiente'}</h1>
            <form onSubmit={salvarAmbiente} style={styles.form}>
              <label style={styles.formGroup}>
                Nome
                <input
                  value={ambienteForm.nome}
                  onChange={(event) => alterarAmbiente('nome', event.target.value)}
                  style={styles.input}
                />
              </label>

              <label style={styles.formGroup}>
                Capacidade
                <input
                  type="number"
                  min="1"
                  value={ambienteForm.capacidade}
                  onChange={(event) => alterarAmbiente('capacidade', event.target.value)}
                  style={styles.input}
                />
              </label>

              <label style={styles.formGroup}>
                Ficha tecnica
                <textarea
                  value={ambienteForm.fichaTecnica}
                  onChange={(event) => alterarAmbiente('fichaTecnica', event.target.value)}
                  style={{ ...styles.input, minHeight: 110 }}
                />
              </label>

              <div style={styles.actions}>
                <button type="submit" style={styles.primaryButton}>
                  {ambienteEditandoId ? 'Atualizar' : 'Criar'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setAmbienteForm(ambienteInicial);
                    setAmbienteEditandoId(null);
                  }}
                  style={styles.secondaryButton}
                >
                  Limpar
                </button>
              </div>
            </form>
          </section>
        )}

        {paginaAtual === 'ambientes' && (
          <section style={styles.panel}>
            <h1 style={styles.title}>Ambientes</h1>
            <form onSubmit={consultarDisponibilidade} style={styles.availabilityForm}>
              <label style={styles.formGroup}>
                Data
                <input
                  type="date"
                  value={disponibilidadeFiltro.data}
                  onChange={(event) => alterarDisponibilidade('data', event.target.value)}
                  style={styles.input}
                />
              </label>

              <label style={styles.formGroup}>
                Horario inicial
                <input
                  type="time"
                  value={disponibilidadeFiltro.horarioInicio}
                  onChange={(event) => alterarDisponibilidade('horarioInicio', event.target.value)}
                  style={styles.input}
                />
              </label>

              <label style={styles.formGroup}>
                Horario final
                <input
                  type="time"
                  value={disponibilidadeFiltro.horarioFim}
                  onChange={(event) => alterarDisponibilidade('horarioFim', event.target.value)}
                  style={styles.input}
                />
              </label>

              <button
                type="submit"
                disabled={disponibilidadeCarregando}
                style={{
                  ...styles.primaryButton,
                  ...(disponibilidadeCarregando ? styles.disabledButton : {})
                }}
              >
                {disponibilidadeCarregando ? 'Consultando...' : 'Ver disponibilidade'}
              </button>
            </form>

            <div style={styles.grid}>
              {ambientes.map((ambiente) => (
                <article key={ambiente.id} style={styles.card}>
                  <div style={styles.cardHeader}>
                    <h2 style={styles.cardTitle}>{ambiente.nome}</h2>
                    {disponibilidadePorAmbiente[ambiente.id] && (
                      <span
                        style={{
                          ...styles.availabilityBadge,
                          ...(disponibilidadePorAmbiente[ambiente.id].disponivel
                            ? styles.availabilityAvailable
                            : styles.availabilityBusy)
                        }}
                      >
                        {disponibilidadePorAmbiente[ambiente.id].disponivel
                          ? 'Disponivel'
                          : 'Ocupado'}
                      </span>
                    )}
                  </div>
                  <p>Capacidade: {ambiente.capacidade}</p>
                  <p style={styles.cardText}>{ambiente.fichaTecnica || 'Sem ficha tecnica'}</p>
                  {disponibilidadePorAmbiente[ambiente.id] && (
                    <p style={styles.muted}>
                      {disponibilidadePorAmbiente[ambiente.id].data} das{' '}
                      {disponibilidadePorAmbiente[ambiente.id].horarioInicio} ate{' '}
                      {disponibilidadePorAmbiente[ambiente.id].horarioFim}
                    </p>
                  )}
                  {isAdmin && (
                    <div style={styles.actions}>
                      <button
                        onClick={() => editarAmbiente(ambiente)}
                        style={styles.secondaryButton}
                      >
                        Editar
                      </button>

                      <button
                        onClick={() => deletarAmbiente(ambiente.id)}
                        style={styles.dangerButton}
                      >
                        Excluir
                      </button>
                    </div>
                  )}
                </article>
              ))}
            </div>
          </section>
        )}

        {paginaAtual === 'notificacoes' && (
          <section style={styles.panel}>
            <h1 style={styles.title}>Notificacoes</h1>
            <div style={styles.list}>
              {notificacoes.map((notificacao) => (
                <article
                  key={notificacao.id}
                  style={{
                    ...styles.notification,
                    ...(notificacao.lida ? {} : styles.notificationUnread)
                  }}
                >
                  <div>
                    <strong>{notificacao.mensagem}</strong>
                    <p style={styles.muted}>{formatarData(notificacao.dataHora)}</p>
                  </div>
                  {!notificacao.lida && (
                    <button onClick={() => marcarComoLida(notificacao.id)} style={styles.secondaryButton}>
                      Marcar como lida
                    </button>
                  )}
                </article>
              ))}
              {!notificacoes.length && <p>Nenhuma notificacao encontrada.</p>}
            </div>
          </section>
        )}

        {paginaAtual === 'usuarios' && isAdmin && (
          <section style={styles.panel}>
            <h1 style={styles.title}>Usuarios</h1>
            <div style={styles.tableWrap}>
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th style={styles.th}>Email</th>
                    <th style={styles.th}>Perfil atual</th>
                    <th style={styles.th}>Alterar perfil</th>
                    <th style={styles.th}>Acoes</th>
                  </tr>
                </thead>
                <tbody>
                  {usuarios.map((usuario) => (
                    <tr key={usuario.id}>
                      <td style={styles.td}>{usuario.email}</td>
                      <td style={styles.td}>
                        <span style={{ ...styles.badge, ...perfilStyle(usuario.role) }}>
                          {usuario.role === 'ADMIN' ? 'ADMIN' : 'USER'}
                        </span>
                      </td>
                      <td style={styles.td}>
                        <select
                          value={usuario.role}
                          onChange={(event) => alterarPerfilUsuario(usuario.id, event.target.value)}
                          disabled={usuario.email === email}
                          style={styles.input}
                        >
                          <option value="USER">Professor</option>
                          <option value="ADMIN">Administrador</option>
                        </select>
                      </td>
                      <td style={styles.td}>
                        <button
                          onClick={() => deletarUsuario(usuario.id)}
                          disabled={usuario.email === email}
                          style={{
                            ...styles.dangerButton,
                            ...(usuario.email === email ? styles.disabledButton : {})
                          }}
                        >
                          Excluir
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {paginaAtual === 'mapa-diario' && isAdmin && (
          <section style={styles.panel}>
            <h1 style={styles.title}>Mapa Diario de Reservas</h1>
            <form onSubmit={gerarMapaDiario} style={styles.form}>
              <label style={styles.formGroup}>
                Data do mapa
                <input
                  type="date"
                  value={mapaData}
                  onChange={(event) => setMapaData(event.target.value)}
                  style={styles.input}
                />
              </label>

              <div style={styles.actions}>
                <button
                  type="submit"
                  disabled={mapaCarregando}
                  style={{
                    ...styles.primaryButton,
                    ...(mapaCarregando ? styles.disabledButton : {})
                  }}
                >
                  {mapaCarregando ? 'Gerando...' : 'Gerar Mapa Diario'}
                </button>
              </div>
            </form>
          </section>
        )}
      </main>
    </div>
  );
}

function Metric({ label, value }) {
  return (
    <div style={styles.metric}>
      <span style={styles.metricValue}>{value}</span>
      <span style={styles.muted}>{label}</span>
    </div>
  );
}

function TabelaReservas({
  reservas,
  isAdmin,
  usuarioId,
  mostrarEdicao = false,
  mostrarExclusao = false,
  mostrarAprovacao = false,
  onEditar,
  onDeletar,
  onStatus
}) {
  if (!reservas.length) {
    return <p>Nenhuma reserva encontrada.</p>;
  }

  return (
    <div style={styles.tableWrap}>
      <table style={styles.table}>
        <thead>
          <tr>
            <th style={styles.th}>Ambiente</th>
            <th style={styles.th}>Professor</th>
            <th style={styles.th}>Data</th>
            <th style={styles.th}>Horario</th>
            <th style={styles.th}>Status</th>
            <th style={styles.th}>Acoes</th>
          </tr>
        </thead>
        <tbody>
          {reservas.map((reserva) => (
            <tr key={reserva.id}>
              <td style={styles.td}>{reserva.ambiente?.nome}</td>
              <td style={styles.td}>{reserva.professor}</td>
              <td style={styles.td}>{reserva.data}</td>
              <td style={styles.td}>{formatarHorarioReserva(reserva)}</td>
              <td style={styles.td}>
                <span style={{ ...styles.badge, ...statusStyle(reserva.status) }}>{reserva.status}</span>
              </td>
              <td style={styles.td}>
                <div style={styles.actions}>
                 {mostrarEdicao &&
                    (isAdmin || reserva.usuario?.id === usuarioId) &&
                    reserva.status !== 'APROVADA' &&
                    reserva.status !== 'REJEITADA' && (
                    <button
                      onClick={() => onEditar(reserva)}
                      style={styles.secondaryButton}
                    >
                      Editar
                    </button>
                  )}
                  {mostrarAprovacao && reserva.status === 'PENDENTE' && (
                    <>
                      <button onClick={() => onStatus(reserva.id, 'APROVADA')} style={styles.primaryButton}>Aprovar</button>
                      <button onClick={() => onStatus(reserva.id, 'REJEITADA')} style={styles.warningButton}>Rejeitar</button>
                    </>
                  )}
                  {mostrarExclusao && (
                    <button onClick={() => onDeletar(reserva.id)} style={styles.dangerButton}>Excluir</button>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function formatarHorarioReserva(reserva) {
  if (!reserva.horarioInicio || !reserva.horarioFim) {
    return '-';
  }

  return `${reserva.horarioInicio} - ${reserva.horarioFim}`;
}

function statusStyle(status) {
  if (status === 'APROVADA') {
    return { backgroundColor: '#dcfce7', color: '#166534' };
  }

  if (status === 'REJEITADA') {
    return { backgroundColor: '#fee2e2', color: '#991b1b' };
  }

  return { backgroundColor: '#fef3c7', color: '#92400e' };
}

function perfilStyle(role) {
  if (role === 'ADMIN') {
    return { backgroundColor: '#e0e7ff', color: '#3730a3' };
  }

  return { backgroundColor: '#f3f4f6', color: '#374151' };
}

function formatarData(data) {
  if (!data) {
    return '';
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short'
  }).format(new Date(data));
}

async function obterMensagemErroPdf(error) {
  const data = error.response?.data;

  if (data instanceof Blob) {
    const texto = await data.text();

    try {
      const json = JSON.parse(texto);
      return json.message;
    } catch {
      return texto;
    }
  }

  return data?.message || data;
}

const styles = {
  container: {
    display: 'flex',
    minHeight: '100vh',
    backgroundColor: '#ecf0f1',
    color: '#2c3e50',
    fontFamily: 'Segoe UI, Tahoma, Geneva, Verdana, sans-serif'
  },
  sidebar: {
    width: 250,
    flexShrink: 0,
    backgroundColor: '#F97316',
    color: 'white',
    padding: '20px 0'
  },
  logo: {
    display: 'flex',
    flexDirection: 'column',
    gap: 8,
    padding: '0 20px 20px 20px',
    marginBottom: 20,
    borderBottom: '1px solid #e06512',
    fontSize: 20
  },
  userInfo: {
    color: 'white',
    fontSize: 13,
    opacity: 0.9,
    overflowWrap: 'anywhere'
  },
  role: {
    alignSelf: 'flex-start',
    border: '1px solid rgba(255,255,255,0.45)',
    borderRadius: 4,
    padding: '4px 8px',
    fontSize: 12
  },
  logout: {
    backgroundColor: '#dc2626',
    color: 'white',
    border: 0,
    borderRadius: 4,
    padding: '10px 12px',
    cursor: 'pointer'
  },
  menuItem: {
    width: '100%',
    display: 'block',
    marginBottom: 0,
    padding: '12px 20px',
    border: 0,
    borderRadius: 0,
    backgroundColor: 'transparent',
    color: 'white',
    textAlign: 'left',
    cursor: 'pointer'
  },
  menuItemActive: {
    backgroundColor: '#696969',
    borderLeft: '4px solid #555555',
    color: 'white'
  },
  content: {
    flex: 1,
    backgroundColor: '#ecf0f1',
    padding: 30,
    overflowX: 'auto'
  },
  panel: {
    backgroundColor: 'white',
    border: 0,
    borderRadius: 8,
    padding: 25,
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
  },
  title: {
    margin: '0 0 20px',
    fontSize: 24,
    color: '#2c3e50'
  },
  metrics: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
    gap: 14
  },
  metric: {
    border: '1px solid #e5e7eb',
    borderRadius: 8,
    padding: 16
  },
  metricValue: {
    display: 'block',
    fontSize: 28,
    fontWeight: 700
  },
  muted: {
    color: '#6b7280',
    fontSize: 13,
    margin: 0
  },
  form: {
    display: 'grid',
    gap: 16,
    maxWidth: 720
  },
  availabilityForm: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
    gap: 12,
    alignItems: 'end',
    marginBottom: 18,
    padding: 14,
    border: '1px solid #e5e7eb',
    borderRadius: 8,
    backgroundColor: '#f9fafb'
  },
  filterForm: {
    display: 'grid',
    gridTemplateColumns: 'minmax(180px, 260px) auto',
    gap: 12,
    alignItems: 'end',
    marginBottom: 14,
    padding: 14,
    border: '1px solid #e5e7eb',
    borderRadius: 8,
    backgroundColor: '#f9fafb'
  },
  formGroup: {
    display: 'grid',
    gap: 6,
    fontWeight: 600
  },
  input: {
    width: '100%',
    boxSizing: 'border-box',
    border: '1px solid #d1d5db',
    borderRadius: 4,
    padding: '11px 12px',
    font: 'inherit'
  },
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: 8,
    alignItems: 'center'
  },
  primaryButton: {
    backgroundColor: '#F97316',
    color: 'white',
    border: 0,
    borderRadius: 4,
    padding: '9px 12px',
    cursor: 'pointer'
  },
  secondaryButton: {
    backgroundColor: '#e5e7eb',
    color: '#111827',
    border: 0,
    borderRadius: 4,
    padding: '9px 12px',
    cursor: 'pointer'
  },
  warningButton: {
    backgroundColor: '#f59e0b',
    color: 'white',
    border: 0,
    borderRadius: 4,
    padding: '9px 12px',
    cursor: 'pointer'
  },
  dangerButton: {
    backgroundColor: '#dc2626',
    color: 'white',
    border: 0,
    borderRadius: 4,
    padding: '9px 12px',
    cursor: 'pointer'
  },
  disabledButton: {
    opacity: 0.55,
    cursor: 'not-allowed'
  },
  tableWrap: {
    overflowX: 'auto'
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse'
  },
  th: {
    padding: 12,
    textAlign: 'left',
    borderBottom: '2px solid #e5e7eb',
    backgroundColor: '#f9fafb'
  },
  td: {
    padding: 12,
    borderBottom: '1px solid #e5e7eb',
    verticalAlign: 'top'
  },
  badge: {
    display: 'inline-block',
    minWidth: 90,
    textAlign: 'center',
    borderRadius: 4,
    padding: '5px 8px',
    fontSize: 12,
    fontWeight: 700
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
    gap: 16
  },
  card: {
    border: '1px solid #e5e7eb',
    borderRadius: 8,
    padding: 16
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: 10,
    marginBottom: 8
  },
  cardTitle: {
    margin: 0,
    fontSize: 18
  },
  cardText: {
    color: '#4b5563',
    minHeight: 44
  },
  availabilityBadge: {
    flexShrink: 0,
    borderRadius: 4,
    padding: '5px 8px',
    fontSize: 12,
    fontWeight: 700
  },
  availabilityAvailable: {
    backgroundColor: '#dcfce7',
    color: '#166534'
  },
  availabilityBusy: {
    backgroundColor: '#fee2e2',
    color: '#991b1b'
  },
  list: {
    display: 'grid',
    gap: 12
  },
  notification: {
    display: 'flex',
    justifyContent: 'space-between',
    gap: 12,
    border: '1px solid #e5e7eb',
    borderRadius: 8,
    padding: 14
  },
  notificationUnread: {
    borderColor: '#f97316',
    backgroundColor: '#fff7ed'
  },
  erro: {
    backgroundColor: '#fee2e2',
    color: '#991b1b',
    borderRadius: 6,
    padding: 12,
    marginBottom: 14
  },
  sucesso: {
    backgroundColor: '#dcfce7',
    color: '#166534',
    borderRadius: 6,
    padding: 12,
    marginBottom: 14
  }
};

export default Home;
