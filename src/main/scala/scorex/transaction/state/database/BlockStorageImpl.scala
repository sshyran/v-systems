package scorex.transaction.state.database

import java.io.File

import com.wavesplatform.settings.BlockchainSettings
import com.wavesplatform.state2.reader.StateReader
import com.wavesplatform.state2.{BlockchainUpdaterImpl, MVStoreStateStorage, StateWriterImpl}
import org.h2.mvstore.MVStore
import scorex.transaction._
import scorex.transaction.state.database.blockchain.{CheckpointServiceImpl, HistoryWriterImpl, MVStoreCheckpointStorage, MVStoreHistoryStorage}

class BlockStorageImpl(settings: BlockchainSettings) extends BlockStorage {

  import BlockStorageImpl._

  val hdatabase: MVStore = createMVStore("history-" + settings.file)
  val sdatabase: MVStore = createMVStore("state-" + settings.file)
  val cdatabase: MVStore = createMVStore("checkpoint-" + settings.file)
  val h = new HistoryWriterImpl(new MVStoreHistoryStorage(hdatabase))
  val s = new StateWriterImpl(new MVStoreStateStorage(sdatabase))
  val c = new CheckpointServiceImpl(new MVStoreCheckpointStorage(cdatabase))
  val updater = new BlockchainUpdaterImpl(s, settings.functionalitySettings, h)


  override def history: History = h

  override def stateReader: StateReader = updater.currentState

  override def blockchainUpdater: BlockchainUpdater = updater

  override def checkpoints: CheckpointService = c
}

object BlockStorageImpl {

  private def stringToOption(s: String) = Option(s).filter(_.trim.nonEmpty)

  def createMVStore(fileName: String): MVStore = {
    stringToOption(fileName) match {
      case Some(s) =>
        val file = new File(s)
        file.getParentFile.mkdirs().ensuring(file.getParentFile.exists())

        new MVStore.Builder().fileName(s).compress().open()
      case None =>
        new MVStore.Builder().open()
    }
  }
}
