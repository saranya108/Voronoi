Voronoi
=======

Implementation of a Voronoi-based ANN query processing algorithm along with previously designed and implemented AANN algorithm.

The Voronoi-based algorithm to solve aggregate nearest neighbor queries (shortened as VANN, described in http://dl.acm.org/citation.cfm?id=1869876) has been implemented along with the
AANN algorithm as a comparitive study of their query processing times. VANN uses the Voronoi-based KNN query processing algorithm 
described in (http://www.vldb.org/conf/2004/RS21P6.PDF). The following are the files related to it:
1) PreComputeVoronoi.java - Has the logic for precomputing the voronoi cells based on a given road network graph
2) VANNQueryProcessing.java - Has the query processing logic
