package amp.topology.core.repo.inmem;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.topology.core.model.ClusterRegistration;
import amp.topology.core.repo.ClusterRegistrationRepository;

/**
 * In-Memory implementation of the ClusterRepository.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class InMemoryClusterRegistrationRepository implements ClusterRegistrationRepository {

	private static final Logger logger = LoggerFactory.getLogger(InMemoryClusterRegistrationRepository.class);
	
	protected ConcurrentHashMap<String, ClusterRegistration> clusters = new ConcurrentHashMap<String, ClusterRegistration>();
	
	
	public void setClusters(Collection<ClusterRegistration> clusters){
		
		for (ClusterRegistration cluster : clusters){
			
			this.clusters.put(cluster.getId(), cluster);
		}
	}
	
	
	@Override
	public ClusterRegistration get(String clusterId) {
		
		logger.debug("Getting cluster by id: {}", clusterId);
		
		return clusters.get(clusterId);
	}

	@Override
	public void create(ClusterRegistration cluster) {
		
		logger.debug("Creating cluster with id: {} and description: {}", cluster.getId(), cluster.getDescription());
		
		this.clusters.putIfAbsent(cluster.getId(), cluster);
	}

	@Override
	public void delete(String clusterId) {
		
		logger.debug("Deleting cluster with id: {}", clusterId);
		
		this.clusters.remove(clusterId);
	}

	@Override
	public void update(ClusterRegistration cluster) {
		
		logger.debug("Updating cluster with id: {}", cluster.getId());
		
		this.clusters.replace(cluster.getId(), cluster);
	}

	@Override
	public Collection<ClusterRegistration> all() {
		
		logger.debug("Getting all clusters.");
		
		return this.clusters.values();
	}

}