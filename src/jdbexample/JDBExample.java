package jdbexample;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * Creates a sample project that connects to a database.
 * @author Anthony Barszcz
 */
public class JDBExample {
    //create components
    private static JTable dbTable;
    private static JLabel lblName, lblType, lblCost, lblCustRating;
    private static JTextField txtName, txtType, txtCost, txtCustRating;
    
    public static void main(String[] args) {
	//declare db constants
	final String DB_URL = "jdbc:mysql://sql.computerstudi.es:3306/gc200318107";
	final String QRY = "SELECT * FROM donuts";

	//declare data retrieval variables
	Connection conn = null;
	Statement stmt = null;
	ResultSet result = null;
	
	//create login window components
	JPanel loginPanel = new JPanel();
	JLabel lblUserName = new JLabel("User Name:"),
		lblPassword = new JLabel("Password:");
	JTextField txtUserName = new JTextField(10);
	JPasswordField txtPassword = new JPasswordField(10);
	
	//add components to the login panel
	loginPanel.add(lblUserName);
	loginPanel.add(txtUserName);
	loginPanel.add(lblPassword);
	loginPanel.add(txtPassword);
        
        //login JOptionPane
        String[] buttonOptions = new String[]{"OK", "Cancel"};
        JOptionPane.showOptionDialog(null, loginPanel, "Enter Login Credentials",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, buttonOptions, buttonOptions[0]);
        
        boolean dbCredCheck = false;
        int loopCount = 0;
        
        //log in
        do {
            try {
		//USERNAME:	gc200318107
		//PASSWORD:	Vn6JKNta
		conn = DriverManager.getConnection(DB_URL, txtUserName.getText(),
			new String(txtPassword.getPassword()));
                dbCredCheck = true;
            } catch(SQLException ExSql) {
                if(loopCount >= 2) {
                    JOptionPane.showMessageDialog(null, "Failed to authenticate.");
                    dbCredCheck = false;
                    loopCount++;
                }
            }
        } while(!dbCredCheck);
        
	//if they logged in successfully
        if(conn != null) {
            try {
		//execute the query and save the results
                stmt = conn.createStatement();
                result = stmt.executeQuery(QRY);
		//validate the result
		if(result != null) {
		    //create JFrame
		    JFrame frame = new JFrame("Java Database Tables");

		    //create main window components
		    JPanel tablePanel = new JPanel(), southPanel = new JPanel();
		    
		    JLabel lblItemSelected = new JLabel("Selected Items:");
		    JTextField txtItemSelected = new JTextField(10);

		    //set up the frame
		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    frame.setSize(500,400);
		    frame.setLayout(new BorderLayout());

		    /* create the view */

		    //create a JTable
		    dbTable = new JTable(buildTBModel(result));
		    
		    //create south panel components
		    lblName = new JLabel("Name:");
		    lblType = new JLabel("Type:");
		    lblCost = new JLabel("Cost:");
		    lblCustRating = new JLabel("Customer Rating:");
		    
		    txtName = new JTextField(10);
		    txtType = new JTextField(10);
		    txtCost = new JTextField(10);
		    txtCustRating = new JTextField(10);
		    
		    //create list selection functionality
		    ListSelectionModel listSelectionModel = dbTable.getSelectionModel();
		    listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());
		    dbTable.setSelectionModel(listSelectionModel);
		    
		    //create JScrollPane containing the dbTable
		    JScrollPane scrollPane = new JScrollPane(dbTable);
		    //add the scroll panel to the table panel
		    tablePanel.add(scrollPane);

		    //add the tablepanel to the JFrame
		    frame.add(tablePanel, BorderLayout.CENTER);
		    
		    //create field panel
		    JPanel fieldPanel = new JPanel(new GridLayout(4,2));
		    
		    //add components to the field panel
		    fieldPanel.add(lblName);
		    fieldPanel.add(txtName);
		    fieldPanel.add(lblType);
		    fieldPanel.add(txtType);
		    fieldPanel.add(lblCost);
		    fieldPanel.add(txtCost);
		    fieldPanel.add(lblCustRating);
		    fieldPanel.add(txtCustRating);
		    
		    //add the fieldPanel to the southPanel
		    southPanel.add(fieldPanel);
		    
		    //add the south panel
		    frame.add(southPanel, BorderLayout.SOUTH);

		    //make the frame visible
		    frame.setVisible(true);
		} else {
		    JOptionPane.showMessageDialog(null, "No results from database",
			    "Error", JOptionPane.ERROR_MESSAGE);
		}
            } catch(SQLException ExSql) {
                //handle error
                System.out.println(ExSql.toString());
            }
        }
    } //end main
    
    //should be in a service class but placing it here due to tmie constraints
    public static DefaultTableModel buildTBModel(ResultSet result)
        throws SQLException {
        ResultSetMetaData metaData = result.getMetaData();
        
        Vector<String> colNames = new Vector<String>();
        
        for(int column = 1; column <= metaData.getColumnCount(); column++) {
            colNames.add(metaData.getColumnName(column));
        }
        
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        
        //go through the resultset
        while(result.next()) {
            Vector<Object> rowData = new Vector<Object>();
            
            for(int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
                rowData.add(result.getObject(columnIndex));
            }
            
            data.add(rowData);
        }
        
        return new DefaultTableModel(data, colNames);
    }
    
    //list selection listener
    private static class SharedListSelectionHandler implements ListSelectionListener {
	public void valueChanged(ListSelectionEvent e) {
	  ListSelectionModel lsm = (ListSelectionModel) e.getSource();

	  if (lsm.isSelectionEmpty()) {
	    System.out.println("No Selection");
	  } else {
	    //find out which indexes are selected.
	    int minIndex = lsm.getMinSelectionIndex();
	    int maxIndex = lsm.getMaxSelectionIndex();

	    for (int i = minIndex; i <= maxIndex; i++) {
	      if (lsm.isSelectedIndex(i)) {
		  //get info from dbTable
		  txtName.setText(dbTable.getValueAt(i, 0).toString());
		  txtType.setText(dbTable.getValueAt(i, 1).toString());
		  txtCost.setText(dbTable.getValueAt(i, 2).toString());
		  txtCustRating.setText(dbTable.getValueAt(i, 3).toString());
	      }
	    }
	  }
	}
    }
} //end class