package bulletin_board.domain.entity

trait Entity[T] {

  /**
    * 対象の文字列が入力されているか否かをチェックする
    * @param target チェック対象の文字列
    * @return 入力されている場合はtrue
    */
  @deprecated(message = "`isNonEmpty`を使用すること")
  protected def isInputEmpty(target: String): Boolean =
    if (target == null || target.isEmpty)
      true
    else
      false

  /**
    * 対象の文字列が入力されているか否かをチェックする
    * @param target チェック対象の文字列
    * @return 入力されている場合はtrue
    */
  protected def isNonEmpty(target: String): Boolean =
    if (target == null || target.isEmpty)
      false
    else
      true

  /**
    * 対象の文字数がN文字以上であるかをチェックする
    * @param target チェック対象の文字列
    * @param n `target`がn文字以上であるかをチェックする
    * @return n文字以上の場合はtrue
    */
  protected def isMoreThan(target: String, n: Int): Boolean =
    if (target.length >= n)
      true
    else
      false

  /**
    * 対象の文字数がN文字以下であるかをチェックする
    * @param target チェック対象の文字列
    * @param n `target`がn文字以下であるかをチェックする
    * @return n文字以上の場合はtrue
    */
  protected def isLessThan(target: String, n: Int): Boolean =
    if (target.length <= n)
      true
    else
      false

}
