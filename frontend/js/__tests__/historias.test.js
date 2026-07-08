const { loadFrontendModules } = require('./moduleHelpers');

describe('HistoriasModule', () => {
  let HistoriasModule;
  let HistoriasAPI;

  beforeEach(() => {
    document.body.innerHTML = `
      <button id="btn-nueva-historia"></button>
      <form id="historia-form">
        <select id="historia-paciente"></select>
        <select id="historia-doctor"></select>
        <textarea id="historia-diagnostico"></textarea>
        <textarea id="historia-tratamiento"></textarea>
        <textarea id="historia-observaciones"></textarea>
      </form>
      <div id="modal-historia"></div>
      <table id="historias-table"><tbody></tbody></table>
    `;

    const modules = loadFrontendModules();
    HistoriasModule = modules.HistoriasModule;
    HistoriasAPI = modules.api.HistoriasAPI;
    global.showAlert = modules.showAlertMock;
  });

  test('renderTabla muestra historias clinicas en la tabla', () => {
    HistoriasModule.renderTabla([
      {
        id: 1,
        paciente: { nombre: 'Ana', apellido: 'Lopez' },
        doctor: { nombre: 'Carlos', apellido: 'Perez' },
        fechaCreacion: '2026-07-08T10:00:00Z',
        diagnostico: 'Dolor de cabeza',
      },
    ]);

    expect(document.querySelector('#historias-table tbody').innerHTML).toContain('Ana Lopez');
    expect(document.querySelector('#historias-table tbody').innerHTML).toContain('Dolor de cabeza');
  });

  test('mostrarFormulario llena los selects con pacientes y doctores', () => {
    HistoriasModule.pacientesCache = [
      { id: 1, nombre: 'Ana', apellido: 'Lopez' },
    ];
    HistoriasModule.doctoresCache = [
      { id: 2, nombre: 'Carlos', apellido: 'Perez' },
    ];

    HistoriasModule.mostrarFormulario();

    expect(document.getElementById('historia-paciente').innerHTML).toContain('Ana Lopez');
    expect(document.getElementById('historia-doctor').innerHTML).toContain('Carlos Perez');
    expect(document.getElementById('modal-historia').classList.contains('show')).toBe(true);
  });

  test('guardarHistoria crea una historia clinica', async () => {
    HistoriasAPI.crear = jest.fn().mockResolvedValue({ id: 1 });
    HistoriasModule.cerrarFormulario = jest.fn();
    HistoriasModule.cargarHistorias = jest.fn().mockResolvedValue();

    document.getElementById('historia-paciente').innerHTML = '<option value="1" selected>Paciente</option>';
    document.getElementById('historia-doctor').innerHTML = '<option value="2" selected>Doctor</option>';
    document.getElementById('historia-diagnostico').value = 'Dolor de cabeza';
    document.getElementById('historia-tratamiento').value = 'Paracetamol';
    document.getElementById('historia-observaciones').value = 'Revisar en 1 semana';

    await HistoriasModule.guardarHistoria({ preventDefault: jest.fn() });

    expect(HistoriasAPI.crear).toHaveBeenCalledWith(expect.objectContaining({ diagnostico: 'Dolor de cabeza' }));
    expect(global.showAlert).toHaveBeenCalledWith('Historia clinica creada exitosamente', 'success');
  });

  test('verHistoria abre el modal con el detalle de la historia', async () => {
    HistoriasAPI.buscar = jest.fn().mockResolvedValue({
      id: 1,
      paciente: { nombre: 'Ana', apellido: 'Lopez' },
      doctor: { nombre: 'Carlos', apellido: 'Perez' },
      fechaCreacion: '2026-07-08T10:00:00Z',
      diagnostico: 'Dolor de cabeza',
      tratamiento: 'Paracetamol',
      observaciones: 'Revisar',
    });

    await HistoriasModule.verHistoria(1);

    expect(document.getElementById('modal-historia').innerHTML).toContain('Dolor de cabeza');
    expect(document.getElementById('modal-historia').classList.contains('show')).toBe(true);
  });
});