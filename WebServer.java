import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.StringTokenizer;



public final class WebServer {

	public static void main(String args[]) throws Exception {
		try (ServerSocket socket = new ServerSocket(6789)) {
      while(true) {
      	Socket s=socket.accept();
      	HttpRequest http=new HttpRequest(s);
      	Thread thread=new Thread(http);
      	thread.start();//启动线程
      }
    }

	}

}

final class HttpRequest implements Runnable {
	final static String CRLF="\r\n"; //“回车与换行”，用于在http响应报文中
	Socket socket;
	public HttpRequest(Socket socket)throws Exception {
		this.socket=socket;
	}
	
	public void run() {
		try {
			requestprocess();
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void requestprocess() throws Exception{
		BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));//将从套接字得到的字节输入流，利用转换流，转换成字符流 ，便于之后更好的读取
		DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
		
		
		String requestLine=br.readLine();  //得到请求行
		System.out.println(requestLine);
		
		String headerLine;
		while((headerLine=br.readLine()).length()!=0) {     
			System.out.println(headerLine);
		}
		
		
		StringTokenizer stokizer=new StringTokenizer(requestLine); //该类用于分解字符串，默认分隔符有：空格，回车，换行等
		stokizer.nextToken();
		String fileName="D:\\大二下半学期\\计算机网络\\多线程Web服务器"+stokizer.nextToken(); //在后面寻找文件中，将会在D盘的根目录进行寻找
		
		FileInputStream fis=null;
		boolean fileIsExisted=true;
		try {
			fis=new FileInputStream(fileName);  //利用这个类相当于进行了寻找，若没有找到，则会捕获异常。
		}catch(FileNotFoundException e1) {
			fileIsExisted=false;
		}
		
		 String statusLine = null;        //状态行
	     String contentTypeLine = null;   //Content-type行
	     Date date=new Date();
	     String dates="Date: "+date.toString()+CRLF; //Date行
	     String entityBody = null;       //实体部分
		if(fileIsExisted) {
			statusLine="HTTP/1.1 200 OK"+CRLF;
			if(fileName.endsWith(".html") || fileName.endsWith(".htm") || fileName.endsWith(".txt")) {
				contentTypeLine="Content-type: text/html;charset=utf-8"+CRLF;
			}
			else if(fileName.endsWith(".jpg")) {
				contentTypeLine="Content-type: image/jpg;charset=utf-8"+CRLF;
			}
			else if(fileName.endsWith(".gif")) {
				contentTypeLine="Content-type: image/gif;charset=utf-8"+CRLF;
			}
			else {
				contentTypeLine="Content-type: Unknown;charset=utf-8"+CRLF;
			}
			
			dos.writeBytes(statusLine);   
			dos.writeBytes(contentTypeLine);
			dos.writeBytes(dates);
			dos.writeBytes(CRLF);
			while(fis.available()>0) {
				byte by[]=new byte[1024];
				fis.read(by);
				dos.write(by);
				
			}//将我本地的文件内容写入到套接字的输出流中，也就是将我的文件传给浏览器
			
		}else {
			statusLine="HTTP/1.1 404 NotFound"+CRLF;
        	contentTypeLine = "Content-type: text/html;charset=utf-8"+CRLF;
        	entityBody ="<html><title>Not found</title><h1>404 NotFound</h1></html>";
        	dos.writeBytes(statusLine);
			dos.writeBytes(contentTypeLine);
			dos.writeBytes(dates);
			dos.writeBytes(CRLF);
			dos.writeBytes(entityBody);
		}
		
		fis.close();
		dos.close();
		br.close();
		socket.close();
	}
	

}


