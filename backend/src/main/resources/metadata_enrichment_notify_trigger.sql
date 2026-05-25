CREATE OR REPLACE FUNCTION notify_metadata_enrichment() RETURNS trigger AS $$
BEGIN
  -- Only notify if the new item is PENDING
  IF NEW.queue_status = 'PENDING' THEN
    PERFORM pg_notify('metadata_enrichment_channel', NEW.id::text);
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS metadata_enrichment_queue_insert_trigger ON metadata_enrichment_queue;

CREATE TRIGGER metadata_enrichment_queue_insert_trigger
AFTER INSERT ON metadata_enrichment_queue
FOR EACH ROW EXECUTE FUNCTION notify_metadata_enrichment();
