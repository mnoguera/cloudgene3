package cloudgene.mapred.util;

import java.io.Serial;

public class GitHubException extends Exception  {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public GitHubException(String message) {
		super(message);
	}

}
