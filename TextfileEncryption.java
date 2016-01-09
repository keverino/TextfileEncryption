// Author: Kevin Lee
// Tuesday, December 2, 2014
// Description:
// This program is designed to encrypt and decrypt text files.
// Each encrypted file has its own AES key saved in the program directory.
// If the key is lost, the program cannot decrypt your file.

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import javax.crypto.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//----------------------------------------------------------------------------------------------------
class TextfileEncryption implements ActionListener
{
   private StopWatch sw;
   private JFrame jfrm;
   private JTextArea textArea;
   private JFileChooser fileChooser;
   private JLabel statusLabel;
   private String versionString = "0.4";
//----------------------------------------------------------------------------------------------------
   public static void main(String args[]) { new TextfileEncryption(); }
//----------------------------------------------------------------------------------------------------
   public TextfileEncryption()
   {
      // Create a new JFrame container with specified settings.
      jfrm = new JFrame("Textfile Encryption");
      jfrm.setLayout(new BorderLayout());
      jfrm.setSize(600, 550);
      jfrm.setLocationRelativeTo(null);
      jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      jfrm.setResizable(false);

      // Create a file chooser object
      fileChooser = new JFileChooser();

      // Create banner
      ImageIcon banner = new ImageIcon(getClass().getResource("images/encrypttext.jpg"));
      JLabel imgLabel = new JLabel();
      imgLabel.setIcon(banner);

      // Create buttons
      JButton encryptButton = new JButton("Encrypt");
      JButton decryptButton = new JButton("Decrypt");
      JButton helpButton = new JButton("Help");

      // Create a text area within a scrollpane
      textArea = new JTextArea(19, 46);
      JScrollPane scrollPane = new JScrollPane(textArea);
      textArea.setEditable(false);

      // Add action listeners.
      encryptButton.addActionListener(this);
      decryptButton.addActionListener(this);
      helpButton.addActionListener(this);

      // Add status label
      statusLabel = new JLabel();

      // Create a new panel
      JPanel panel = new JPanel(new FlowLayout());
      panel.add(imgLabel);
      panel.add(encryptButton);
      panel.add(decryptButton);
      panel.add(helpButton);
      panel.add(scrollPane);

      // Add objects to the content pane.
      jfrm.add(panel, BorderLayout.CENTER);
      jfrm.add(statusLabel, BorderLayout.SOUTH);

      //Display the frame.
      jfrm.setVisible(true);
   }
//----------------------------------------------------------------------------------------------------
   public void actionPerformed(ActionEvent ae)
   {
      try
      {
         if(ae.getActionCommand().equals("Encrypt"))
         {
            fileChooser.setDialogTitle("Choose a text file to encrypt");
            int returnVal = fileChooser.showOpenDialog(jfrm);
            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
               jfrm.setTitle("Textfile Encryption - " + fileChooser.getSelectedFile().getName());
            }
            encryptText();
            fileChooser.setSelectedFile(new File(""));
         }
         else if(ae.getActionCommand().equals("Decrypt"))
         {
            fileChooser.setDialogTitle("Choose a text file to decrypt");
            int returnVal = fileChooser.showOpenDialog(jfrm);
            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
               jfrm.setTitle("Textfile Encryption - " + fileChooser.getSelectedFile().getName());
            }
            decryptText();
            fileChooser.setSelectedFile(new File(""));
         }
         else if (ae.getActionCommand().equals("Help"))
         {
           textArea.setText("");
           statusLabel.setText("");
           jfrm.setTitle("Textfile Encryption");
           JOptionPane.showMessageDialog(jfrm,
            "Version: " + versionString + "\n\n"
            + "Description: \n"
            + "This program is designed to encrypt and decrypt text files.\n"
            + "More specifically, it is designed to work with files ending in .txt\n\n"
            + "Each encrypted file will have its own AES key generated in the same directory.\n"
            + "If the key is lost, the program cannot decrypt your file.\n"
            + "Note: The key must be in the same directory as the file you're trying to decrypt.\n",
            "Help / About", JOptionPane.PLAIN_MESSAGE);
         }
      }//end try
   catch (Exception e){}
   }
//----------------------------------------------------------------------------------------------------
   public void encryptText() throws Exception
   {
      String fileName = fileChooser.getSelectedFile().getName();
      fileName = fileName.replace(".txt", "");
      String fullPath = fileChooser.getSelectedFile().getAbsolutePath();
      fullPath = fullPath.replace(".txt", "");

      // clear the text area
      textArea.setText("");
      statusLabel.setText("");

      // generate a new AES session key
      sw = new StopWatch();
      textArea.append("Generating new AES session key...");
      AESKey sessionKey = new AESKey();
      Key currentSessionKey = sessionKey.getKey();
      textArea.append("DONE\n");
      textArea.append("[ " + sw.elapsedTime() + "s ]\n");

      // save the key to a file
      sw = new StopWatch();
      textArea.append("Saving key...");
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fullPath + "-key.bin"));
      oos.writeObject(currentSessionKey);
      oos.close();
      textArea.append("DONE\n");
      textArea.append("[ " + sw.elapsedTime() + "s ]\n");

      // read file to a string
      sw = new StopWatch();
      textArea.append("Reading text from " + fileName + ".txt...");
      String entireFileText = new Scanner(new File(fullPath + ".txt")).useDelimiter("\\A").next();
      byte[] plainText = entireFileText.getBytes();
      textArea.append("DONE\n");
      textArea.append("[ " + sw.elapsedTime() + "s ]\n");

      // encrypt the text with AES
      sw = new StopWatch();
      textArea.append("Encrypting text...");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, currentSessionKey);
      byte[] encryptedText = cipher.doFinal(plainText);
      textArea.append("DONE\n");
      textArea.append("[ " + sw.elapsedTime() + "s ]\n");

      // save the encrypted text
      sw = new StopWatch();
      textArea.append("Saving encrypted text...");
      oos = new ObjectOutputStream(new FileOutputStream(fullPath + ".txt"));
      oos.writeObject(encryptedText);
      oos.close();
      textArea.append("DONE\n");
      textArea.append("[ " + sw.elapsedTime() + "s ]\n\n");

      JOptionPane.showMessageDialog(jfrm, "Your file is now entirely encrypted.", "Success!", JOptionPane.PLAIN_MESSAGE);
   }
//----------------------------------------------------------------------------------------------------
   public void decryptText() throws Exception
   {
      String fileName = fileChooser.getSelectedFile().getName();
      fileName = fileName.replace(".txt", "");
      String fullPath = fileChooser.getSelectedFile().getAbsolutePath();
      fullPath = fullPath.replace(".txt", "");

      // clear the text area
      textArea.setText("");
      statusLabel.setText("");

      // load the key from file
      sw = new StopWatch();
      textArea.append("Reading key...");
      File publicKeyFile = new File(fullPath + "-key.bin");
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(publicKeyFile));
      Key publicKey = (Key) ois.readObject();
      ois.close();
      textArea.append("DONE\n");
      textArea.append("[ " + sw.elapsedTime() + "s ]\n");

      // read file to byte array
      sw = new StopWatch();
      textArea.append("Reading encrypted text from " + fileName + ".txt...");
      ois = new ObjectInputStream(new FileInputStream(fullPath + ".txt"));
      byte[] encryptedText = (byte[]) ois.readObject();
      ois.close();
      textArea.append("DONE\n");
      textArea.append("[ " + sw.elapsedTime() + "s ]\n");

      // decrypt the text
      sw = new StopWatch();
      textArea.append("Decrypting text...");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, publicKey);
      byte[] decryptedText = cipher.doFinal(encryptedText);
      textArea.append("DONE\n");
      //textArea.append(new String(decryptedText));
      textArea.append("[ " + sw.elapsedTime() + "s ]\n");

      // save the decrypted text
      sw = new StopWatch();
      textArea.append("Saving decrypted text...");
      PrintWriter out  = new PrintWriter(fullPath + ".txt");
      out.print(new String(decryptedText));
      out.close();
      textArea.append("DONE\n");
      textArea.append("[ " + sw.elapsedTime() + "s ]\n");

      // delete the key
      sw = new StopWatch();
      textArea.append("Deleting key...");
      publicKeyFile.delete();
      textArea.append("DONE\n");
      textArea.append("[ " + sw.elapsedTime() + "s ]\n\n");

      JOptionPane.showMessageDialog(jfrm, "Your file has successfully been decrypted.", "Success!", JOptionPane.PLAIN_MESSAGE);
   }
//----------------------------------------------------------------------------------------------------
}// end of TextfileEncryption class
//----------------------------------------------------------------------------------------------------
class StopWatch
{
   private double start;
//---------------------------------------------------------------------------------------------------
   public StopWatch() { start = System.currentTimeMillis(); }
//----------------------------------------------------------------------------------------------------
   public double elapsedTime() { return ( (System.currentTimeMillis() - start) / 1000 ); }
//----------------------------------------------------------------------------------------------------
}//end of StopWatch class
//----------------------------------------------------------------------------------------------------
class AESKey implements Serializable
{
   Key sessionKey;
   byte[] serializedKeyBytes;
//----------------------------------------------------------------------------------------------------
   public AESKey() throws Exception
   {
      // create AES session key
      sessionKey = KeyGenerator.getInstance("AES").generateKey();

      try
      {
         // serialize the AES session key
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(bos);
         out.writeObject(sessionKey);
         serializedKeyBytes = bos.toByteArray();
         out.close();
         bos.close();
      }
      catch (IOException e) { System.out.println("Error trying to serialize the AES key"); }
   }
//----------------------------------------------------------------------------------------------------
   public Key getKey() { return sessionKey; }
//----------------------------------------------------------------------------------------------------
   public byte[] getSerializedKeyBytes() { return serializedKeyBytes; }
//----------------------------------------------------------------------------------------------------
}// end of AESKey class
//----------------------------------------------------------------------------------------------------
