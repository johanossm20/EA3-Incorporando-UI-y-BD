import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Aplicación de escritorio que permite ingresar los datos de una base de datos
 * y probar la conexión mediante JDBC. Muestra en pantalla el resultado
 * (éxito en verde, error en rojo).
 *
 * Unidad 3 - Programación Orientada a Objetos II.
 */
public class ConexionTester extends JFrame {

    private static final Color COLOR_EXITO = new Color(0, 128, 0);
    private static final Color COLOR_ERROR = new Color(192, 0, 0);

    private JTextField campoUrl;
    private JTextField campoUsuario;
    private JPasswordField campoClave;
    private JButton botonProbar;
    private JTextArea areaResultado;

    public ConexionTester() {
        super("Probador de conexión a base de datos");
        construirInterfaz();
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz gráfica
    // ------------------------------------------------------------------

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(crearPanelFormulario(), BorderLayout.NORTH);
        add(crearPanelResultado(), BorderLayout.CENTER);

        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setMinimumSize(new Dimension(520, 360));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Datos de conexión"));

        campoUrl = new JTextField("jdbc:sqlite:inventario.db");
        campoUsuario = new JTextField();
        campoClave = new JPasswordField();
        botonProbar = new JButton("Probar conexión");

        botonProbar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                probarConexion();
            }
        });

        panel.add(new JLabel("URL de conexión:"));
        panel.add(campoUrl);
        panel.add(new JLabel("Usuario:"));
        panel.add(campoUsuario);
        panel.add(new JLabel("Contraseña:"));
        panel.add(campoClave);
        panel.add(new JLabel(""));
        panel.add(botonProbar);
        return panel;
    }

    private JScrollPane crearPanelResultado() {
        areaResultado = new JTextArea(8, 40);
        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(areaResultado);
        scroll.setBorder(BorderFactory.createTitledBorder("Resultado de la prueba"));
        return scroll;
    }

    // ------------------------------------------------------------------
    // Lógica de la prueba de conexión
    // ------------------------------------------------------------------

    /** Se ejecuta al presionar el botón: lee los campos y muestra el resultado. */
    private void probarConexion() {
        String url = campoUrl.getText().trim();
        String usuario = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());

        if (url.isEmpty()) {
            mostrarMensaje("Debe ingresar la URL de conexión.", COLOR_ERROR);
            return;
        }

        try {
            String detalle = validarConexion(url, usuario, clave);
            mostrarMensaje("Conexión exitosa.\n" + detalle, COLOR_EXITO);
        } catch (ClassNotFoundException e) {
            mostrarMensaje("Fallo del driver: no se encontró la clase "
                    + e.getMessage() + ".\nVerifique que el .jar del driver "
                    + "esté en el classpath.", COLOR_ERROR);
        } catch (SQLException e) {
            mostrarMensaje("Fallo de conexión.\nDetalle técnico: "
                    + e.getMessage(), COLOR_ERROR);
        }
    }

    /**
     * Carga el driver y abre la conexión con try-with-resources, de modo que
     * se cierre sola tanto si todo sale bien como si ocurre un error.
     *
     * @return una descripción del motor al que se conectó
     */
    private String validarConexion(String url, String usuario, String clave)
            throws ClassNotFoundException, SQLException {

        Class.forName(obtenerClaseDriver(url));

        try (Connection conexion = DriverManager.getConnection(url, usuario, clave)) {
            DatabaseMetaData info = conexion.getMetaData();
            return "Motor: " + info.getDatabaseProductName()
                    + " " + info.getDatabaseProductVersion();
        }
    }

    /** Devuelve el nombre de la clase del driver según el tipo de URL. */
    private String obtenerClaseDriver(String url) {
        if (url.startsWith("jdbc:sqlite:")) {
            return "org.sqlite.JDBC";
        } else if (url.startsWith("jdbc:mysql:")) {
            return "com.mysql.cj.jdbc.Driver";
        } else if (url.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        }
        return "driver.desconocido";
    }

    private void mostrarMensaje(String mensaje, Color color) {
        areaResultado.setForeground(color);
        areaResultado.setText(mensaje);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ConexionTester().setVisible(true);
            }
        });
    }
}
