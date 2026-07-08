const { loadScript } = require('./testHelpers');

describe('App', () => {
  let App;
  let PacientesAPI;
  let DoctoresAPI;
  let CitasAPI;

  beforeEach(() => {
    document.body.innerHTML = `
      <button class="nav-btn active" data-section="dashboard">Dashboard</button>
      <button class="nav-btn" data-section="pacientes">Pacientes</button>
      <button class="nav-btn" data-section="doctores">Doctores</button>
      <button class="nav-btn" data-section="citas">Citas</button>
      <button class="nav-btn" data-section="historias">Historias</button>

      <section id="section-dashboard" class="section active"></section>
      <section id="section-pacientes" class="section"></section>
      <section id="section-doctores" class="section"></section>
      <section id="section-citas" class="section"></section>
      <section id="section-historias" class="section"></section>

      <div id="stat-total-pacientes"></div>
      <div id="stat-total-doctores"></div>
      <div id="stat-citas-hoy"></div>
      <div id="stat-edad-promedio"></div>
    `;

    global.PacientesModule = { init: jest.fn().mockResolvedValue() };
    global.DoctoresModule = { init: jest.fn().mockResolvedValue() };
    global.CitasModule = { init: jest.fn().mockResolvedValue() };
    global.HistoriasModule = { init: jest.fn().mockResolvedValue() };

    PacientesAPI = {
      listar: jest.fn().mockResolvedValue([{ id: 1 }, { id: 2 }]),
      edadPromedio: jest.fn().mockResolvedValue(27.4),
    };
    DoctoresAPI = {
      listar: jest.fn().mockResolvedValue([{ id: 3 }]),
    };
    CitasAPI = {
      listar: jest.fn().mockResolvedValue([
        { id: 1, fechaHora: '2026-07-08T10:30:00.000Z' },
        { id: 2, fechaHora: '2026-07-07T10:30:00.000Z' },
      ]),
    };

    const loaded = loadScript('app.js', ['App'], {
      PacientesAPI,
      DoctoresAPI,
      CitasAPI,
      PacientesModule: global.PacientesModule,
      DoctoresModule: global.DoctoresModule,
      CitasModule: global.CitasModule,
      HistoriasModule: global.HistoriasModule,
    });

    App = loaded.App;
  });

  test('init llama setupNavigation y carga el dashboard', async () => {
    App.setupNavigation = jest.fn();
    App.cargarDashboard = jest.fn().mockResolvedValue();

    await App.init();

    expect(App.setupNavigation).toHaveBeenCalled();
    expect(App.cargarDashboard).toHaveBeenCalled();
  });

  test('navegarA activa la seccion correcta y llama al modulo correspondiente', async () => {
    App.cargarDashboard = jest.fn().mockResolvedValue();

    await App.navegarA('pacientes');

    expect(App.currentSection).toBe('pacientes');
    expect(document.querySelector('[data-section="pacientes"]').classList.contains('active')).toBe(true);
    expect(document.querySelector('#section-pacientes').classList.contains('active')).toBe(true);
    expect(global.PacientesModule.init).toHaveBeenCalled();
  });

  test('cargarDashboard actualiza las estadisticas con los datos recibidos', async () => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2026-07-08T12:00:00Z'));

    await App.cargarDashboard();

    expect(PacientesAPI.listar).toHaveBeenCalled();
    expect(DoctoresAPI.listar).toHaveBeenCalled();
    expect(CitasAPI.listar).toHaveBeenCalled();
    expect(document.getElementById('stat-total-pacientes').textContent).toBe('2');
    expect(document.getElementById('stat-total-doctores').textContent).toBe('1');
    expect(document.getElementById('stat-citas-hoy').textContent).toBe('1');
    expect(document.getElementById('stat-edad-promedio').textContent).toBe('27.4 años');
  });

  test('setupNavigation adjunta el manejador de click sobre los botones', () => {
    App.navegarA = jest.fn();

    App.setupNavigation();

    document.querySelector('[data-section="citas"]').click();

    expect(App.navegarA).toHaveBeenCalledWith('citas');
  });
});