package amp.topology.core.factory.rmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.topology.core.factory.TopologyFactory;
import amp.topology.core.model.Cluster;
import amp.topology.core.model.ClusterDoesntExistException;
import amp.topology.core.repo.ClusterRepository;

public class RmqClusterFactory implements TopologyFactory<Cluster> {

	private static final Logger logger = LoggerFactory.getLogger(RmqClusterFactory.class);
	
	ClusterRepository clusterRepository;
	
	public RmqClusterFactory(ClusterRepository clusterRepository){
		
		this.clusterRepository = clusterRepository;
	}
	
	@Override
	public String getFactoryName() {
		
		return this.getClass().getCanonicalName();
	}

	@Override
	public Cluster build(String context) {
		
		Cluster cluster = null;
		
		try {
			
			cluster = clusterRepository.get(context);
			
		} catch (ClusterDoesntExistException e) {
			
			logger.error("Could not find cluster: {}", e);
		}
		
		return cluster;
	}

}
