package br.uefs.ecomp.cliente.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente {
	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket cliente = new Socket("127.0.0.1", 12345);
		System.out.println("O cliente se conectou ao servidor!");
		
		DataOutputStream outputDados = new DataOutputStream(cliente.getOutputStream());
		DataInputStream inputDados = new DataInputStream(cliente.getInputStream());
		
		String pacote = menu();
		System.out.println(pacote);
		outputDados.writeUTF(pacote);	
		if(inputDados.readInt() == 2) {
			System.out.println("Cadastro Concluido");
		}
		outputDados.close();
		inputDados.close();
	}
	
	public static String formataCadastroConta(Integer acao, String nome, Boolean eJuridica, String numeroRegistro) {
		return acao.toString()+"-"+nome+";"+eJuridica.toString()+";"+numeroRegistro; 
	}
	
	public static String menu() {
		Scanner scanner = new Scanner(System.in);
		String pacote;
			while(true) {
				System.out.println("Escolha a op��o desejada");
				System.out.println("1 - Cadastrar Conta");
				System.out.println("Vai sair e?");
				
				
				switch (scanner.nextInt()) {
				case 1:
					
					System.out.println("Digite o seu nome");
					
					String nome = (String) scanner.next(); // Tratar pra nomes completos
					boolean eJuridica = false;
					
					
					while(true) {
						System.out.println("A conta ser� de pessoa f�sica (1) ou jur�dica (2)?, digite a alternativa correspondente");
						int escolha = scanner.nextInt();
						if(escolha == 1) {
							break;
						} else if(escolha == 2) {
							eJuridica = true;
							break;
						} 
					}
					
					System.out.println("Digite seu n�mero de CPF ou CNPJ, por favor");
					String numeroRegistro = (String) scanner.next();		
					pacote = formataCadastroConta(1, nome, eJuridica, numeroRegistro);
					return pacote;
					//break;
				default:
					System.out.println("Digite uma op��o v�lida");
					break;
				}
			}
			
	}
	
}
