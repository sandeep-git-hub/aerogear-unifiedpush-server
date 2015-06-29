package org.jboss.aerogear.unifiedpush.poolconnection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;

public class SimpleClient {

	private Cluster cluster;
	private PoolingOptions pooling;

	public void connect(String node) throws IOException {
		pooling = new PoolingOptions();
		cluster = Cluster.builder().addContactPoint(node).withPoolingOptions(pooling).build();
		Properties props = new Properties();
		InputStream in = new FileInputStream("src/main/resources/config.properties");
		props.load(in);
		System.out.println("props are " + props.getProperty("pool_timeout"));
		pooling.setCoreConnectionsPerHost(HostDistance.LOCAL,
				Integer.parseInt(props.getProperty("coreConn_hostDistance_local")))
				.setMaxConnectionsPerHost(HostDistance.LOCAL,
						Integer.parseInt(props.getProperty("maxcConn_hostDistance_local")))
				.setCoreConnectionsPerHost(HostDistance.REMOTE,
						Integer.parseInt(props.getProperty("coreConn_hostDistance_remote")))
				.setMaxConnectionsPerHost(HostDistance.REMOTE,
						Integer.parseInt(props.getProperty("maxConn_hostDistance_remote")));
		pooling.setMaxSimultaneousRequestsPerHostThreshold(HostDistance.LOCAL,
				Integer.parseInt(props.getProperty("maxRequest_local"))).setMaxSimultaneousRequestsPerHostThreshold(
						HostDistance.REMOTE, Integer.parseInt(props.getProperty("maxRequest_local")));
		pooling.setHeartbeatIntervalSeconds(60);
		pooling.setPoolTimeoutMillis(6000);

		System.out.println("Pooling is " + pooling);
		System.out.println("cluster is " + cluster + "--" + cluster.getDriverVersion());

		Session session = cluster.connect();
		System.out.println("Sesison is " + session + "----" + session.getState());
		// cluster = Cluster.builder().addContactPoint(node).build();
		Metadata metadata = cluster.getMetadata();
		System.out.println("Metadata is " + metadata.getClusterName());
		for (Host host : metadata.getAllHosts()) {
			System.out.printf("Datacenter %s \n Host %s \n Rack %s \n", host.getDatacenter(), host.getAddress(),
					host.getRack());
		}
	}

	public void close() {
		cluster.close();
		System.out.println("closed successfully");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleClient client = new SimpleClient();
		try {
			client.connect("127.0.0.1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.close();
	}

}
