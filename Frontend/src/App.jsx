import { useState, useEffect } from 'react';
import './App.css';
import api from './api';

function App() {
  const [activeTab, setActiveTab] = useState('create');
  const [solicitudes, setSolicitudes] = useState([]);
  const [formData, setFormData] = useState({
    tipo: '',
    prioridadManual: '',
    usuario: ''
  });
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [serverStatus, setServerStatus] = useState('checking'); // checking, connected, disconnected
  const [filters, setFilters] = useState({
    tipo: '',
    prioridad: '',
    usuario: ''
  });

  // Revisar si el servidor está activo al cargar la app
  useEffect(() => {
    checkServerHealth();
    const interval = setInterval(checkServerHealth, 100000); // Revisamos cada 100 segundos
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (activeTab === 'list') {
      fetchList('/Listar');
    } else if (activeTab === 'prioritized') {
      fetchList('/Listar/Priorizado');
    }
  }, [activeTab]);

  const checkServerHealth = async () => {
    try {
      await api.get('/api/health');
      setServerStatus('connected');
    } catch (error) {
      setServerStatus('disconnected');
      console.error("Server health check failed", error);
    }
  };

  const fetchList = async (endpoint) => {
    setIsLoading(true);
    try {
      const response = await api.get(endpoint);
      setSolicitudes(response.data);
      setMessage('');
    } catch (error) {
      console.error("Error fetching data", error);
      setMessage("Error de conexión. Verifica que el backend esté corriendo en el puerto 8080.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    
    // Validar y limpiar nombre de usuario
    const usuarioTrimmed = formData.usuario.trim();
    if (!usuarioTrimmed) {
      setMessage('Error: El nombre de usuario no puede estar vacío');
      setTimeout(() => setMessage(''), 3000);
      return;
    }
    
    // Validar que se haya seleccionado un tipo
    if (!formData.tipo) {
      setMessage('Error: Debes seleccionar un tipo de solicitud');
      setTimeout(() => setMessage(''), 3000);
      return;
    }
    
    // Validar que se haya ingresado una prioridad
    if (formData.prioridadManual === '' || formData.prioridadManual < 1 || formData.prioridadManual > 5) {
      setMessage('Error: Debes ingresar una prioridad entre 1 y 5');
      setTimeout(() => setMessage(''), 3000);
      return;
    }
    
    try {
      // Enviar con el usuario limpio
      await api.post('/Solicitud/Crear', {
        ...formData,
        usuario: usuarioTrimmed
      });
      setMessage('Solicitud creada exitosamente');
      setFormData({ tipo: '', prioridadManual: '', usuario: '' });
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      console.error("Error creating solicitud", error);
      setMessage("Error al crear la solicitud. Verifica la conexión con el servidor.");
    }
  };

  const handleNumberChange = (e) => {
    const val = e.target.value;
    if (val === '') {
      setFormData({ ...formData, prioridadManual: '' });
    } else {
      const num = parseInt(val);
      if (num >= 1 && num <= 5) {
        setFormData({ ...formData, prioridadManual: num });
      }
    }
  };

  const formatId = (id) => {
    return String(id).padStart(4, '0');
  };

  const getPriorityClass = (prioridad) => {
    if (prioridad >= 4) return 'priority-high';
    if (prioridad === 3) return 'priority-medium';
    return 'priority-low';
  };

  const getFilteredSolicitudes = () => {
    return solicitudes.filter(item => {
      const sol = item.solicitud ? item.solicitud : item;
      
      if (filters.tipo && sol.tipo !== filters.tipo) return false;
      if (filters.prioridad) {
        const prioridad = parseInt(filters.prioridad);
        if (sol.prioridadManual !== prioridad) return false;
      }
      // Buscar por ID o usuario
      if (filters.usuario) {
        const searchTerm = filters.usuario.toLowerCase();
        const matchesId = formatId(sol.id).includes(searchTerm) || String(sol.id).includes(searchTerm);
        const matchesUsuario = sol.usuario.toLowerCase().includes(searchTerm);
        if (!matchesId && !matchesUsuario) return false;
      }
      
      return true;
    });
  };

  const clearFilters = () => {
    setFilters({ tipo: '', prioridad: '', usuario: '' });
  };

  return (
    <div className="app">
      <div className="sidebar">
        <div className="logo">
          <h1>Prueba Tecnica</h1>
          <p className="subtitle">Motor de priorización</p>
        </div>

        <nav className="nav">
          <button
            onClick={() => setActiveTab('create')}
            className={`nav-item ${activeTab === 'create' ? 'active' : ''}`}
          >
            <span className="nav-icon">+</span>
            <span>Nueva Solicitud</span>
          </button>
          <button
            onClick={() => setActiveTab('list')}
            className={`nav-item ${activeTab === 'list' ? 'active' : ''}`}
          >
            <span className="nav-icon">≡</span>
            <span>Todas</span>
          </button>
          <button
            onClick={() => setActiveTab('prioritized')}
            className={`nav-item ${activeTab === 'prioritized' ? 'active' : ''}`}
          >
            <span className="nav-icon">★</span>
            <span>Priorizadas</span>
          </button>
        </nav>
      </div>

      <div className="main-content">
        <header className="header">
          <h2>
            {activeTab === 'create' && 'Nueva Solicitud'}
            {activeTab === 'list' && 'Todas las Solicitudes'}
            {activeTab === 'prioritized' && 'Solicitudes Priorizadas'}
          </h2>

          <div className={`server-status ${serverStatus}`}>
            <span className="status-dot"></span>
            <span className="status-text">
              {serverStatus === 'connected' && 'Conectado'}
              {serverStatus === 'disconnected' && 'Desconectado'}
              {serverStatus === 'checking' && 'Verificando...'}
            </span>
          </div>
        </header>

        <div className="content">
          {message && (
            <div className={`alert ${message.includes('Error') ? 'error' : 'success'}`}>
              {message}
            </div>
          )}

          {activeTab === 'create' && (
            <div className="form-container">
              <form onSubmit={handleCreate}>
                <div className="form-group">
                  <label>Nombre de Usuario</label>
                  <input
                    type="text"
                    placeholder="Ingrese el nombre del usuario"
                    value={formData.usuario}
                    onChange={e => setFormData({ ...formData, usuario: e.target.value })}
                    required
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Tipo de Solicitud</label>
                    <select
                      value={formData.tipo}
                      onChange={e => setFormData({ ...formData, tipo: e.target.value })}
                      required
                    >
                      <option value="" disabled>Seleccione un tipo...</option>
                      <option value="INCIDENTE">Incidente</option>
                      <option value="REQUERIMIENTO">Requerimiento</option>
                      <option value="CONSULTA">Consulta</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label>Prioridad Manual (1-5)</label>
                    <input
                      type="number"
                      min="1"
                      max="5"
                      placeholder="Ingrese prioridad (1-5)"
                      value={formData.prioridadManual}
                      onChange={handleNumberChange}
                      required
                    />
                  </div>
                </div>

                <button type="submit" className="btn-primary">
                  Crear Solicitud
                </button>
              </form>
            </div>
          )}

          {(activeTab === 'list' || activeTab === 'prioritized') && (
            <div className="table-container">
              <div className="filters-section">
                <div className="filters-row">
                  <div className="filter-group">
                    <label>Buscar por ID o Usuario</label>
                    <input
                      type="text"
                      placeholder="ID o nombre del usuario..."
                      value={filters.usuario}
                      onChange={e => setFilters({ ...filters, usuario: e.target.value })}
                    />
                  </div>
                  
                  <div className="filter-group">
                    <label>Tipo</label>
                    <select
                      value={filters.tipo}
                      onChange={e => setFilters({ ...filters, tipo: e.target.value })}
                    >
                      <option value="">Todos</option>
                      <option value="INCIDENTE">Incidente</option>
                      <option value="REQUERIMIENTO">Requerimiento</option>
                      <option value="CONSULTA">Consulta</option>
                    </select>
                  </div>

                  <div className="filter-group">
                    <label>Prioridad Manual</label>
                    <select
                      value={filters.prioridad}
                      onChange={e => setFilters({ ...filters, prioridad: e.target.value })}
                    >
                      <option value="">Todas</option>
                      <option value="1">1 - Muy Baja</option>
                      <option value="2">2 - Baja</option>
                      <option value="3">3 - Media</option>
                      <option value="4">4 - Alta</option>
                      <option value="5">5 - Crítica</option>
                    </select>
                  </div>

                  <div className="filter-actions">
                    <button className="btn-clear" onClick={clearFilters}>Limpiar</button>
                    <button
                      className="btn-secondary"
                      onClick={() => fetchList(activeTab === 'list' ? '/Listar' : '/Listar/Priorizado')}
                    >
                      Actualizar
                    </button>
                  </div>
                </div>
              </div>

              <div className="table-actions">
                <span className="results-count">
                  Mostrando {getFilteredSolicitudes().length} de {solicitudes.length} solicitudes
                </span>
              </div>

              {isLoading ? (
                <div className="loading">Cargando datos...</div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Usuario</th>
                      <th>Tipo</th>
                      <th>Prioridad Manual</th>
                      <th>Fecha de Creación</th>
                    </tr>
                  </thead>
                  <tbody>
                    {getFilteredSolicitudes().map((item) => {
                      const sol = item.solicitud ? item.solicitud : item;

                      return (
                        <tr key={sol.id}>
                          <td><span className="id-badge">{formatId(sol.id)}</span></td>
                          <td>{sol.usuario}</td>
                          <td>
                            <span className={`badge ${sol.tipo.toLowerCase()}`}>
                              {sol.tipo}
                            </span>
                          </td>
                          <td>
                            <span className={`priority-badge ${getPriorityClass(sol.prioridadManual)}`}>
                              <span className="priority-indicator"></span>
                              {sol.prioridadManual}
                            </span>
                          </td>
                          <td>{new Date(sol.fechaCreacion).toLocaleString('es-ES')}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}

              {getFilteredSolicitudes().length === 0 && !isLoading && (
                <div className="empty-state">
                  {solicitudes.length === 0 ? 'No hay solicitudes para mostrar' : 'No se encontraron solicitudes con los filtros aplicados'}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;
