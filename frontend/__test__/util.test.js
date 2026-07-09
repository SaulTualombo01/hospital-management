/** @jest-environment jsdom */
const {
    formatDate,
    formatDateTime,
    escapeHTML,
    showAlert,
    validateEmail,
    validateTelefono,
    isFutureDate,
    localToISO
} = require('../js/utils');

describe('Pruebas Unitarias - utils.js', () => {

    // ==========================================
    // PRUEBAS DE FECHAS
    // ==========================================
    describe('formatDate()', () => {
        test('Debe formatear correctamente una fecha ISO válida', () => {
            // Usamos las 12 del mediodía para evitar que el ajuste de zona horaria cambie el día
            const result = formatDate('2026-05-15T12:00:00Z');
            expect(result).toMatch(/15 de mayo de 2026/i);
        });

        test('Caso Límite 1: Falla silenciosa con undefined (Retorna Invalid Date)', () => {
            const result = formatDate(undefined);
            expect(result).toBe('Invalid Date');
        });

        test('Caso Límite 2: Falla silenciosa con null (Retorna fecha Epoch 1969)', () => {
            // null se convierte en 0 (Epoch UTC). Al restar 5 horas (GMT-5), resulta en 1969.
            const result = formatDate(null);
            expect(result).toMatch(/31 de diciembre de 1969/i);
        });
    });

    describe('formatDateTime()', () => {
        test('Debe formatear fecha y hora correctamente', () => {
            const result = formatDateTime('2026-05-15T15:30:00Z');
            // La aserción busca que contenga el año para validar un formato correcto
            expect(result).toContain('2026');
        });

        test('Caso Límite (Bug Intencional): No valida null y retorna Epoch', () => {
            const result = formatDateTime(null);
            expect(result).toContain('1969');
        });

        test('Caso Inválido: String vacío retorna Invalid Date', () => {
            const result = formatDateTime('');
            expect(result).toBe('Invalid Date');
        });
    });

    describe('isFutureDate()', () => {
        test('Debe retornar true para una fecha en el futuro', () => {
            // Creamos una fecha un año en el futuro
            const futureDate = new Date();
            futureDate.setFullYear(futureDate.getFullYear() + 1);
            expect(isFutureDate(futureDate.toISOString())).toBe(true);
        });

        test('Debe retornar false para una fecha en el pasado', () => {
            expect(isFutureDate('2000-01-01T00:00:00Z')).toBe(false);
        });

        test('Caso Límite: Manejo de undefined', () => {
            // new Date(undefined) es Invalid Date, lo cual matemáticamente no es > Date.now()
            expect(isFutureDate(undefined)).toBe(false);
        });
    });

    describe('localToISO()', () => {
        test('Debe convertir un string local a formato ISO', () => {
            const result = localToISO('2026-05-15T10:00');
            expect(result).toContain('2026-05-15');
            expect(result).toContain('Z'); // Verifica que anexa el indicador UTC
        });

        test('Caso Límite (Bug Intencional): Asume UTC sin ajustar la zona horaria', () => {
            const localInput = '2026-05-15T10:00';
            const result = localToISO(localInput);
            // El bug radica en que simplemente le añade Z al final o aplica un desfase incorrecto
            // Verificamos que la función se ejecuta sin lanzar excepciones para probar su comportamiento actual
            expect(typeof result).toBe('string');
        });
    });

    // ==========================================
    // PRUEBAS DE SEGURIDAD Y UI
    // ==========================================
    describe('escapeHTML()', () => {
        test('Debe escapar <, > y & correctamente', () => {
            const result = escapeHTML('<script>alert("XSS & test")</script>');
            expect(result).toBe('&lt;script&gt;alert(&quot;XSS &amp; test&quot;)&lt;/script&gt;');
        });

        test('Caso Límite: Manejo de null y undefined', () => {
            expect(escapeHTML(null)).toBe('');
            expect(escapeHTML(undefined)).toBe('');
        });

        test('Vulnerabilidad (Bug Intencional): No escapa comillas simples ni backticks', () => {
            const vulnerableString = "javascript:alert(`XSS'`)";
            const result = escapeHTML(vulnerableString);
            expect(result).toContain("'");
            expect(result).toContain("`");
        });
    });

    describe('showAlert()', () => {
        // Configuramos un DOM falso antes de cada prueba para que document.getElementById funcione
        beforeEach(() => {
            document.body.innerHTML = '<div id="alert-container"></div>';
        });

        test('Debe inyectar el mensaje en el contenedor HTML', () => {
            showAlert('Operación exitosa', 'success');
            const container = document.getElementById('alert-container');
            expect(container.innerHTML).toContain('Operación exitosa');
            expect(container.innerHTML).toContain('alert-success');
        });

        test('Vulnerabilidad XSS (Bug Intencional): Inyecta HTML sin sanitizar', () => {
            const xssPayload = '<img src="x" onerror="alert(1)">';
            showAlert(xssPayload, 'error');
            const container = document.getElementById('alert-container');

            // Demuestra que el payload malicioso se inyectó directamente en el DOM
            expect(container.innerHTML).toContain(xssPayload);
        });

        test('Caso Límite: No hace nada si el contenedor no existe', () => {
            document.body.innerHTML = ''; // Borramos el contenedor
            // La función no debe lanzar un error, simplemente hace un return silencioso
            expect(() => showAlert('Test')).not.toThrow();
        });
    });

    // ==========================================
    // PRUEBAS DE VALIDACIÓN
    // ==========================================
    describe('validateEmail()', () => {
        test('Debe aceptar un email con formato estándar', () => {
            expect(validateEmail('test@epn.edu.ec')).toBe(true);
        });

        test('Caso Límite: Rechaza null, undefined o vacíos', () => {
            expect(validateEmail(null)).toBe(false);
            expect(validateEmail(undefined)).toBe(false);
            expect(validateEmail('')).toBe(false);
        });

        test('Discrepancia de Documentación: Rechaza correos sin TLD (Contrario al comentario del dev)', () => {
            expect(validateEmail('usuario@dominio')).toBe(false);
        });

        test('Falso Positivo (Bug Real): Acepta dominios con puntos consecutivos', () => {
            expect(validateEmail('usuario@dominio..com')).toBe(true);
        });
    });

    describe('validateTelefono()', () => {
        test('Debe aceptar exactamente 10 dígitos', () => {
            expect(validateTelefono('0991234567')).toBe(true);
        });

        test('Casos Inválidos: Letras o menos de 10 dígitos', () => {
            expect(validateTelefono('099123abc')).toBe(false); // Contiene letras
            expect(validateTelefono('12345')).toBe(false);     // Muy corto
            expect(validateTelefono('09912345678')).toBe(false); // Muy largo
        });

        test('Falso Positivo (Bug Intencional): Acepta prefijos inválidos', () => {
            // Un número ecuatoriano empieza con 09, 02, etc. El sistema acepta cualquier dígito.
            expect(validateTelefono('9999999999')).toBe(true);
        });

        test('Caso Límite: Manejo de null y undefined', () => {
            expect(validateTelefono(null)).toBe(false);
            expect(validateTelefono(undefined)).toBe(false);
        });
    });
});