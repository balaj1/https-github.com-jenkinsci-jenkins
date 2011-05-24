require 'rest_client'

module JenkinsRestAPI
  class BaseExecutor
    def execute!(args, options)
      configure(options)
      if args[0]
        result = send(options[:action], args[0]).to_s
        puts result if !result.nil? && !result.empty?
      else
        send(options[:action])
      end
    end

    def configure(options)
      @server = "#{options[:server_url].chomp('/')}/rest-api"
    end
  end

  class JobExecutor < BaseExecutor
    def configure(options)
      super
      @job_url = "#{@server}/jobs"
    end

    def status(job)
      RestClient.get "#{@job_url}/#{job}"
    end

    def delete(job)
      RestClient.delete "#{@job_url}/#{job}"
    end

    def disable(job)
      RestClient.post "#{@job_url}/#{job}/disable", :accept => 'text/plain'
    end

    def enable(job)
      RestClient.post "#{@job_url}/#{job}/enable", :accept => 'text/plain'
    end

    def build(job)
      RestClient.post "#{@job_url}/#{job}/builds/new", :accept => 'text/plain'
    end

    def list 
      RestClient.get @job_url
    end
  end

  class BuildExecutor < BaseExecutor
    def configure(options)
      super
      @job = options[:job]
      @build_url = "#{@server}/jobs/#{@job}/builds"
    end

    def status(build_number)
      RestClient.get "#{@build_url}/#{build_number}"
    end

    def retain(build_number)
      RestClient.post "#{@build_url}/#{build_number}/retain", :accept => 'text/plain'
    end

    def delete(build_number)
      RestClient.delete "#{@build_url}/#{build_number}"
    end

    def logfile(build_number)
      RestClient.get "#{@build_url}/#{build_number}/logfile"
    end

    def artifacts(build_number)
      RestClient.get "#{@build_url}/#{build_number}/artifacts"
    end

    def list
      RestClient.get @build_url
    end
  end

  class ArtifactExecutor < BaseExecutor
    def configure(options)
      super
      @build_url = "#{@server}/artifacts"
    end

    def list
      RestClient.get @build_url
    end

    def latest
      RestClient.get "#{@build_url}/latest"
    end
  end
end
