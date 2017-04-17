package br.uefs.ecomp.cliente.controller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import br.uefs.ecomp.cliente.exceptions.PessoaExistenteException;
import br.uefs.ecomp.cliente.model.Acao;

public class Cliente {
	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchAlgorithmException {
		Socket cliente = new Socket("127.0.0.1", 12346); // conecta com o servidor
		System.out.println("O cliente se conectou ao servidor!");
		
		DataOutputStream outputDados = new DataOutputStream(cliente.getOutputStream()); //streams de dados
		DataInputStream inputDados = new DataInputStream(cliente.getInputStream());
		
		String pacote = menu(); //recebe pacote com base em menu
		System.out.println(pacote);
		outputDados.writeUTF(pacote);	//envia o pacote ao servidor
		int resposta = inputDados.readInt();
		if(resposta == 1) { //confirma se a operação foi feita corretamente
			System.out.println("Cadastro Concluido");
		} else if(resposta == 10) {
			System.out.println("Cadastro não realizado: Pessoa já existe");
		}
		outputDados.close(); //fecha output streams
		inputDados.close();
	}
	
	public static String formataCadastroConta(Integer acao, String nome, Boolean eJuridica, String numeroRegistro, String cep, String rua, String numero, String usuario, String senha) { //formata o pacote para o cadastro de contas
		return acao.toString()+"-"+nome+";"+eJuridica.toString()+";"+numeroRegistro+";"+cep+";"+rua+";"+numero+";"+usuario+";"+senha; 
	}
	
	public static String menu() throws IOException, NoSuchAlgorithmException {
		Scanner scanner = new Scanner(System.in);
		String pacote;
			while(true) {
				System.out.println("Escolha a opção desejada");
				System.out.println("1 - Cadastrar Conta");
				System.out.println("Vai sair e?");
				
				switch (scanner.nextInt()) {
				case 1:
				
					System.out.println("Digite o seu nome");
					BufferedReader leitor = new BufferedReader(new InputStreamReader(System.in));
					String nome = leitor.readLine();// Tratar exception
					boolean eJuridica = false;
					
					
					while(true) {
						System.out.println("A conta será de pessoa física (1) ou jurídica (2)?, digite a alternativa correspondente");
						int escolha = scanner.nextInt();
						if(escolha == 1) {
							break;
						} else if(escolha == 2) {
							eJuridica = true;
							break;
						} 
					}
					
					System.out.println("Digite seu número de CPF ou CNPJ, por favor");
					String numeroRegistro = (String) scanner.next();
					
					System.out.println("Digite o seu CEP");
					String cep = (String) scanner.next();
					System.out.println("Digite a sua rua"); //tratar para ruas
					String rua = leitor.readLine();
					System.out.println("Digite o numero da casa");
					String numero = (String) scanner.next();
					
					System.out.println("Está quase acabando, agora, escolha um usuário pra sua conta");
					String usuario = (String) scanner.next(); 
					System.out.println("Por último, digite uma senha para sua nova conta no Banco Virtual (sem espaços)");
					String senha = (String) scanner.next();
					
					String senhaMd5 = md5(senha);
					while(true) {
						System.out.println("Digite (1) para conta corrente e (2) para jurídica");
						int escolha = scanner.nextInt();
						if(escolha == 1) {
							pacote = formataCadastroConta(Acao.CADASTRAR_CONTA_CORRENTE, nome, eJuridica, numeroRegistro, cep, rua, numero, usuario, senhaMd5);
							break;
						} else if(escolha == 2) {
							pacote = formataCadastroConta(Acao.CADASTRAR_CONTA_POUPANCA, nome, eJuridica, numeroRegistro, cep, rua, numero, usuario, senhaMd5);
							break;
						}
					}
					return pacote;
					//break;
				default:
					System.out.println("Digite uma opção válida");
					break;
				}
			}
			
	}
	public static String md5(String senha) throws NoSuchAlgorithmException {
		 MessageDigest m=MessageDigest.getInstance("MD5");
	       m.update(senha.getBytes(),0,senha.length());
	      // System.out.println("MD5: "+new BigInteger(1,m.digest()).toString(16));
	      return new BigInteger(1,m.digest()).toString(16);
	}
}
