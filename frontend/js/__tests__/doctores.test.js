const { loadFrontendModules } = require('./moduleHelpers');

describe('DoctoresModule', () => {
  let DoctoresModule;
  let DoctoresAPI;

  beforeEach(() => {
    document.body.innerHTML = `
      <button id="btn-nuevo-doctor"></button>
      <form id="doctor-form">
        <input id="doctor-id" />
        <input id="doctor-nombre" />
        <input id="doctor-apellido" />
        <input id="doctor-especialidad" />
        <input id="doctor-email" />
        <input id="doctor-telefono" />
        <input id="doctor-consultorio" />
      </form>
      <div id="modal-doctor"></div>
      <input id="search-doctores" />
      <table id="doctores-table"><tbody></tbody></table>
    `;

    const modules = loadFrontendModules();
    DoctoresModule = modules.DoctoresModule;
    DoctoresAPI = modules.api.DoctoresAPI;
    global.showAlert = modules.showAlertMock;
  });

  test('renderTabla muestra doctores en la tabla', () => {
    DoctoresModule.renderTabla([
      {
        id: 1,
        nombre: 'Ana',
        apellido: 'Perez',
        especialidad: 'Cardiología',
        email: 'ana@test.com',
        consultorio: 'C1',
      },
    ]);

    expect(document.querySelector('#doctores-table tbody').innerHTML).toContain('Ana Perez');
    expect(document.querySelector('#doctores-table tbody').innerHTML).toContain('Cardiología');
  });

  test('mostrarFormulario llena el modal con datos del doctor', () => {
    DoctoresModule.mostrarFormulario({
      id: 2,
      nombre: 'Carlos',
      apellido: 'Lopez',
      especialidad: 'Pediatría',
      email: 'carlos@test.com',
      telefono: '0991111111',
      consultorio: 'C2',
    });

    expect(document.getElementById('doctor-id').value).toBe('2');
    expect(document.getElementById('doctor-especialidad').value).toBe('Pediatría');
    expect(document.getElementById('modal-doctor').classList.contains('show')).toBe(true);
  });

  test('guardarDoctor crea un doctor nuevo', async () => {
    DoctoresAPI.crear = jest.fn().mockResolvedValue({ id: 1 });
    DoctoresModule.cerrarFormulario = jest.fn();
    DoctoresModule.cargarDoctores = jest.fn().mockResolvedValue();

    document.getElementById('doctor-nombre').value = 'Ana';
    document.getElementById('doctor-apellido').value = 'Perez';
    document.getElementById('doctor-especialidad').value = 'Cardiología';
    document.getElementById('doctor-email').value = 'ana@test.com';
    document.getElementById('doctor-telefono').value = '0991234567';
    document.getElementById('doctor-consultorio').value = 'C1';

    await DoctoresModule.guardarDoctor({ preventDefault: jest.fn() });

    expect(DoctoresAPI.crear).toHaveBeenCalledWith(expect.objectContaining({ especialidad: 'Cardiología' }));
    expect(global.showAlert).toHaveBeenCalledWith('Doctor creado exitosamente', 'success');
  });

  test('eliminarDoctor invoca la API y recarga', async () => {
    DoctoresAPI.eliminar = jest.fn().mockResolvedValue({});
    DoctoresModule.cargarDoctores = jest.fn().mockResolvedValue();

    await DoctoresModule.eliminarDoctor(3);

    expect(DoctoresAPI.eliminar).toHaveBeenCalledWith(3);
    expect(global.showAlert).toHaveBeenCalledWith('Doctor eliminado exitosamente', 'success');
  });
});