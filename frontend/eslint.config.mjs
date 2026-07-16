import js from '@eslint/js';
import globals from 'globals';

const globalesCompartidas = {
  PacientesModule: 'readonly',
  DoctoresModule: 'readonly',
  CitasModule: 'readonly',
  HistoriasModule: 'readonly',

  PacientesAPI: 'readonly',
  DoctoresAPI: 'readonly',
  CitasAPI: 'readonly',
  HistoriasAPI: 'readonly',

  showAlert: 'readonly',
  formatDate: 'readonly',
  formatDateTime: 'readonly',
  localToISO: 'readonly'
};

const nombresGlobalesCompartidos =
    '^(PacientesModule|DoctoresModule|CitasModule|HistoriasModule|' +
    'PacientesAPI|DoctoresAPI|CitasAPI|HistoriasAPI|' +
    'showAlert|formatDate|formatDateTime|localToISO)$';

const archivosPrueba = [
  'js/__tests__/**/*.js',
  'js/**/*.test.js'
];

export default [
  {
    ignores: [
      'node_modules/**',
      'coverage/**',
      'dist/**'
    ]
  },

  js.configs.recommended,

  // Archivos JavaScript usados directamente por el navegador.
  {
    files: ['js/**/*.js'],
    ignores: archivosPrueba,

    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'script',

      globals: {
        ...globals.browser,
        ...globalesCompartidas,

        // Utilizado en las exportaciones condicionales para Jest.
        module: 'readonly'
      }
    },

    rules: {
      // Evita el conflicto entre los globales configurados
      // y sus declaraciones en api.js, utils.js, pacientes.js, etc.
      'no-redeclare': [
        'error',
        {
          builtinGlobals: false
        }
      ],

      'no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^_',
          caughtErrors: 'none',
          varsIgnorePattern: nombresGlobalesCompartidos
        }
      ],

      'no-console': 'off',
      'eqeqeq': ['error', 'always'],
      'curly': ['error', 'all'],
      'semi': ['error', 'always']
    }
  },

  // Archivos de pruebas Jest.
  {
    files: archivosPrueba,

    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'commonjs',

      globals: {
        ...globals.browser,
        ...globals.node,
        ...globals.jest
      }
    },

    rules: {
      // Evita conflictos con los comentarios:
      // /* global describe, beforeEach, test, expect, jest */
      'no-redeclare': [
        'error',
        {
          builtinGlobals: false
        }
      ],

      'no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^_',
          caughtErrors: 'none'
        }
      ],

      'no-console': 'off',
      'eqeqeq': ['error', 'always'],
      'curly': ['error', 'all'],
      'semi': ['error', 'always']
    }
  }
];