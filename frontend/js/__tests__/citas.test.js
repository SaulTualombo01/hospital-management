const { loadFrontendModules } = require('./moduleHelpers');

describe('CitasModule', () => {
  let CitasModule;
  let CitasAPI;
  let DoctoresAPI;
  let PacientesAPI;

  beforeEach(() => {
    document.body.innerHTML = `
      <button id="btn-nueva-cita"></button>
      <form id="cita-form">
        <input id="cita-id" />
        <input id="cita-fecha-hora" />
        <input id="cita-motivo" />
        <input id="cita-estado" />
        <select id="cita-doctor"></select>
        <select id="cita-paciente"></select>
      </form>
      <div id="modal-cita"></div>
      <select id="filter-estado-citas"></select>
      <table id="citas-table"><tbody></tbody></table>
    `;

    const modules = loadFrontendModules();
    CitasModule = modules.CitasModule;
    CitasAPI = modules.api.CitasAPI;
    DoctoresAPI = modules.api.DoctoresAPI;
    PacientesAPI = modules.api.PacientesAPI;
    global.showAlert = modules.showAlertMock;
  });

  test('renderTabla muestra citas en la tabla', () => {
    CitasModule.renderTabla([
      {
        id: 1,
        pacienteId: 10,
        doctor: { id: 2, nombre: 'Ana', apellido: 'Perez' },
        fechaHora: '2026-07-08T10:30:00Z',
        motivo: 'Consulta',
        estado: 'PROGRAMADA',
      },
    ]);

    expect(document.querySelector('#citas-table tbody').innerHTML).toContain('Paciente #10');
    expect(document.querySelector('#citas-table tbody').innerHTML).toContain('Ana Perez');
  });

  test('mostrarFormulario llena los selects con doctores y pacientes', () => {
    CitasModule.doctoresCache = [
      { id: 1, nombre: 'Ana', apellido: 'Perez', especialidad: 'Cardiología' },
    ];
    CitasModule.pacientesCache = [
      { id: 2, nombre: 'Luis', apellido: 'Gomez', activo: true },
    ];

    CitasModule.mostrarFormulario();

    expect(document.getElementById('cita-doctor').innerHTML).toContain('Cardiología');
    expect(document.getElementById('cita-paciente').innerHTML).toContain('Luis Gomez');
    expect(document.getElementById('modal-cita').classList.contains('show')).toBe(true);
  });

  test('guardarCita crea una cita nueva', async () => {
    CitasAPI.crear = jest.fn().mockResolvedValue({ id: 1 });
    CitasModule.cerrarFormulario = jest.fn();
    CitasModule.cargarCitas = jest.fn().mockResolvedValue();

    document.getElementById('cita-id').value = '';
    document.getElementById('cita-paciente').innerHTML = '<option value="2" selected>Paciente</option>';
    document.getElementById('cita-doctor').innerHTML = '<option value="3" selected>Doctor</option>';
    document.getElementById('cita-fecha-hora').value = '2026-07-08T10:30';
    document.getElementById('cita-motivo').value = 'Consulta';
    document.getElementById('cita-estado').value = 'PROGRAMADA';

    await CitasModule.guardarCita({ preventDefault: jest.fn() });

    expect(CitasAPI.crear).toHaveBeenCalledWith(expect.objectContaining({ motivo: 'Consulta' }));
    expect(global.showAlert).toHaveBeenCalledWith('Cita creada exitosamente', 'success');
  });

  test('filtrarPorEstado usa la API cuando se indica un estado', async () => {
    CitasAPI.porEstado = jest.fn().mockResolvedValue([]);
    CitasModule.renderTabla = jest.fn();

    await CitasModule.filtrarPorEstado('PROGRAMADA');

    expect(CitasAPI.porEstado).toHaveBeenCalledWith('PROGRAMADA');
    expect(CitasModule.renderTabla).toHaveBeenCalledWith([]);
  });
});